package com.bubble.crawler.job.zk.queue;

import com.google.common.collect.Maps;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;

/**
 * 基于zk实现的分布式队列
 *
 * @author wugang
 * date: 2020-04-14 10:35
 **/
public class DistributedQueue implements Queue {
    private static final Logger LOG = LoggerFactory.getLogger(DistributedQueue.class);
    private final String dir;
    private ZooKeeper zookeeper;
    private List<ACL> acl = ZooDefs.Ids.OPEN_ACL_UNSAFE;
    private final String prefix = "qn-";

    public DistributedQueue(ZooKeeper zookeeper, String dir, List<ACL> acl) {
        this.zookeeper = zookeeper;
        this.dir = dir;
        if (acl != null) {
            this.acl = acl;
        }
    }

    /**
     * Find the smallest child node.
     *
     * @return The name of the smallest child node.
     */
    private String smallestChildName() throws KeeperException, InterruptedException {
        long minId = Long.MAX_VALUE;
        String minName = "";
        List<String> childNames;
        try {
            childNames = zookeeper.getChildren(dir, false);
        } catch (KeeperException.NoNodeException e) {
            LOG.warn("Unexpected exception", e);
            return null;
        }
        for (String childName : childNames) {
            try {
                //Check format
                if (!childName.regionMatches(0, prefix, 0, prefix.length())) {
                    LOG.warn("Found child node with improper name: {}", childName);
                    continue;
                }
                String suffix = childName.substring(prefix.length());
                // 寻最小值
                long childId = Long.parseLong(suffix);
                if (childId < minId) {
                    minId = childId;
                    minName = childName;
                }
            } catch (NumberFormatException e) {
                LOG.warn("Found child node with improper format : {}", childName, e);
            }
        }
        if (minId < Long.MAX_VALUE) {
            return minName;
        } else {
            return null;
        }
    }

    /**
     * 返回按id排序的子元素的映射。
     *
     * @param watcher 监听事件
     * @return 从id映射到所有子元素的名称
     */
    private Map<Long, String> orderedChildren(Watcher watcher) throws KeeperException, InterruptedException {
        Map<Long, String> orderedChildren = Maps.newTreeMap();
        List<String> childNames = zookeeper.getChildren(dir, watcher);
        for (String childName : childNames) {
            try {
                // Check format 测试两个字符串区域是否相等。
                if (!childName.regionMatches(0, prefix, 0, prefix.length())) {
                    LOG.warn("找到名称不正确的子节点: {}", childName);
                    continue;
                }
                String suffix = childName.substring(prefix.length());
                Long childId = Long.parseLong(suffix);
                orderedChildren.put(childId, childName);
            } catch (NumberFormatException e) {
                LOG.warn("找到名称不正确的子节点: {}", childName, e);
            }
        }
        return orderedChildren;
    }

    private Map<Long, String> checkAndGetOrderedChildren() {
        Map<Long, String> orderedChildren;
        // 如element方法读取到了队列为空的状态，会抛出NoSuchElementException异常
        try {
            orderedChildren = orderedChildren(null);
        } catch (KeeperException e) {
            LOG.error("[{}] getChildren时，服务器使用非零错误代码发出错误信号。无元素返回", dir);
            throw new NoSuchElementException();
        } catch (InterruptedException e) {
            LOG.error("[{}] getChildren时，服务器事务中断。无元素返回", dir);
            throw new NoSuchElementException();
        }
        if (orderedChildren.size() == 0) {
            LOG.error("[{}] getChildren为空。无元素返回", dir);
            throw new NoSuchElementException();
        }
        return orderedChildren;
    }

    /**
     * 在不修改队列的情况下返回队列的头部。
     *
     * @return 队列头部的数据
     */
    @Override
    public byte[] element() {
        while (true) {
            // 如element方法读取到了队列为空的状态，会抛出NoSuchElementException异常
            Map<Long, String> orderedChildren = checkAndGetOrderedChildren();
            // 如有元素返回
            for (String headNode : orderedChildren.values()) {
                if (headNode != null) {
                    try {
                        // 返回的childNames保存的是队列内容的一个快照。
                        // 这个return语句返回快照中还没出队。如果队列快照的元素都出队了，重试。
                        return zookeeper.getData(dir + "/" + headNode, false, null);
                    } catch (InterruptedException | KeeperException e) {
                        // Another client removed the node first, try next
                    }
                }
            }
        }
    }

    /**
     * 删除队列头元素，并返回：
     * remove 方法和 element 方法类似。值得注意的是getData的成功执行不意味着出队成功，
     * 原因是该队列元素可能会被其他用户出队。
     *
     * @return 队列头元素
     */
    @Override
    public byte[] remove() {
        while (true) {
            // 如element方法读取到了队列为空的状态，会抛出NoSuchElementException异常
            Map<Long, String> orderedChildren = checkAndGetOrderedChildren();
            // 如有元素返回
            for (String headNode : orderedChildren.values()) {
                String path = dir + "/" + headNode;
                try {
                    byte[] data = zookeeper.getData(path, false, null);
                    // version为1做无条件删除，非0整数做有条件删除
                    zookeeper.delete(path, -1);
                    return data;
                } catch (InterruptedException | KeeperException e) {
                    // Another client deleted the node first.
                }
            }
        }
    }

    private static class LatchChildWatcher implements Watcher {
        CountDownLatch latch; // 计数器

        public LatchChildWatcher() {
            latch = new CountDownLatch(1);
        }

        public void process(WatchedEvent event) {
            LOG.debug("Watcher fired: {}", event);
            latch.countDown();
        }

        public void await() throws InterruptedException {
            latch.await();
        }

    }

    /**
     * 出队列：删除队列的头并返回它，阻塞直到它成功。
     *
     * @return 队列的头
     */
    @Override
    public byte[] take() {
        Map<Long, String> orderedChildren;
        while (true) {
            // 利用计数器设置count=1来阻塞等待成功
            LatchChildWatcher childWatcher = new LatchChildWatcher();
            // 如element方法读取到了队列为空的状态，会抛出NoSuchElementException异常
            try {
                orderedChildren = orderedChildren(null);
            } catch (InterruptedException | KeeperException e) {
                LOG.error("[{}] getChildren失败，执行create操作", dir);
                try {
                    zookeeper.create(dir, new byte[0], acl, CreateMode.PERSISTENT);
                } catch (KeeperException | InterruptedException ex) {
                    LOG.error("[{}] 执行create操作失败", dir);
                    ex.printStackTrace();
                }
                continue;
            }
            if (orderedChildren.size() == 0) {
                LOG.error("[{}] getChildren为空。阻塞等待", dir);
                try {
                    childWatcher.await();
                } catch (InterruptedException e) {
                    LOG.error("[{}] 阻塞等待Interrupted异常", dir);
                    e.printStackTrace();
                }
            }

            for (String headNode : orderedChildren.values()) {
                String path = dir + "/" + headNode;
                try {
                    byte[] data = zookeeper.getData(path, false, null);
                    zookeeper.delete(path, -1);
                    return data;
                } catch (InterruptedException | KeeperException e) {
                    // Another client deleted the node first.
                }
            }
        }
    }

    /**
     * 插入数据
     *
     * @param data 待插入的数据
     * @return true if data was successfully added
     */
    @Override
    public boolean offer(byte[] data) throws KeeperException, InterruptedException {
        while (true) {
            try {
                zookeeper.create(dir + "/" + prefix, data, acl, CreateMode.PERSISTENT_SEQUENTIAL);
                return true;
            } catch (KeeperException.NoNodeException e) {
                zookeeper.create(dir, new byte[0], acl, CreateMode.PERSISTENT);
            }
        }
    }

    /**
     * 返回队列第一个元素的数据，如果队列为空，则返回null。
     *
     * @return 队列第一个元素的数据
     */
    @Override
    public byte[] peek() {
        return element();
    }

    /**
     * 尝试删除队列的头并返回它。如果队列为空，则返回null。
     *
     * @return 队列头
     */
    @Override
    public byte[] poll() {
        return remove();
    }
}
