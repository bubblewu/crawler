package com.bubble.crawler.job.zk.lock;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.apache.zookeeper.CreateMode.EPHEMERAL_SEQUENTIAL;

/**
 * 一种实现排他写锁或选举Leader的协议
 * 您调用{@link #lock()}来启动获取锁的过程；你可以在那时拿到锁，也可以过一段时间再拿。
 * <p>You invoke {@link #lock()} to start the process of grabbing the lock;
 * you may get the lock then or it may be some time later.
 * <p>
 * 您可以注册一个监听器，以便在获得锁时被调用;否则，您可以通过调用{@link #isOwner()}来询问是否拥有锁。
 * <p>You can register a listener so that you are invoked when you get the lock;
 * otherwise you can ask if you have the lock by calling {@link #isOwner()}.
 */
public class WriteLock extends ProtocolSupport {
    private static final Logger LOG = LoggerFactory.getLogger(WriteLock.class);

    private final String dir;
    private String id; // 节点名：前缀+顺序号
    private ZNodeName idName;
    private String ownerId; // 当前锁的持有者节点
    private String lastChildId;
    private byte[] data = {0x12, 0x34};
    private LockListener callback; // 回调函数：获得/释放锁
    private LockZooKeeperOperation zop; // zk操作的锁

    /**
     * zookeeper contructor for writelock.
     *
     * @param zookeeper zookeeper client instance
     * @param dir       the parent path you want to use for locking
     * @param acl       the acls that you want to use for all the paths, if null world read/write is used.
     */
    public WriteLock(ZooKeeper zookeeper, String dir, List<ACL> acl) {
        super(zookeeper);
        this.dir = dir;
        if (acl != null) {
            setAcl(acl);
        }
        this.zop = new LockZooKeeperOperation();
    }

    /**
     * zookeeper contructor for writelock with callback.
     *
     * @param zookeeper the zookeeper client instance
     * @param dir       the parent path you want to use for locking
     * @param acl       the acls that you want to use for all the paths
     * @param callback  the call back instance
     */
    public WriteLock(ZooKeeper zookeeper, String dir, List<ACL> acl, LockListener callback) {
        this(zookeeper, dir, acl);
        this.callback = callback;
    }

    /**
     * return the current locklistener.
     *
     * @return the locklistener
     */
    public synchronized LockListener getLockListener() {
        return this.callback;
    }

    /**
     * register a different call back listener.
     *
     * @param callback the call back instance
     */
    public synchronized void setLockListener(LockListener callback) {
        this.callback = callback;
    }

    /**
     * 解锁：
     * 如果不再需要锁，则删除锁或关联的znode。
     * 还会删除在队列中的请求，以便在您尚未持有锁的情况下进行锁定。
     *
     * @throws RuntimeException throws a runtime exception if it cannot connect to zookeeper.
     */
    public synchronized void unlock() throws RuntimeException {
        // 如当前zk没关闭，id不为空表示待释放的锁节点依然存在。
        // 就是解锁：删除持有锁的节点 和 锁监听器释放并设置id为空，表示当前为无锁状态
        if (!isClosed() && id != null) {
            // 我们不会在失败的情况下重试此操作，因为ZK会删除临时文件，并且如果我们无法重新连接到ZK，我们也不想在关闭时挂起此进程
            try {
                // 删除当前持有锁的节点，即该id，释放锁成功
                ZooKeeperOperation zopdel = () -> {
                    zookeeper.delete(id, -1);
                    return Boolean.TRUE;
                };
                zopdel.execute();
            } catch (InterruptedException e) {
                LOG.warn("Unexpected exception", e);
                // set that we have been interrupted.
                Thread.currentThread().interrupt();
            } catch (KeeperException.NoNodeException e) {
                // do nothing
            } catch (KeeperException e) {
                LOG.warn("Unexpected exception", e);
                throw new RuntimeException(e.getMessage(), e);
            } finally {
                // 获取当前的锁监听器
                LockListener lockListener = getLockListener();
                // 不为空。释放。id设为空
                if (lockListener != null) {
                    lockListener.lockReleased();
                }
                id = null;
            }
        }
    }

    /**
     * 如节点发生变化，就会执行监听器中的process方法
     */
    private class LockWatcher implements Watcher {

        public void process(WatchedEvent event) {
            // lets either become the leader or watch the new/updated node
            LOG.debug("Watcher fired: {}", event);
            try {
                lock(); // 加锁
            } catch (Exception e) {
                LOG.warn("Failed to acquire lock", e);
            }
        }
    }

    /**
     * a zookeeper operation that is mainly responsible
     * for all the magic required for locking.
     */
    private class LockZooKeeperOperation implements ZooKeeperOperation {

        /**
         * 查找是否创建了earlier，如果没有创建该prefix节点前缀
         *
         * @param prefix    the prefix node 前缀节点
         * @param zookeeper teh zookeeper client
         * @param dir       父级ZNode
         * @throws KeeperException
         * @throws InterruptedException
         */
        private void findPrefixInChildren(String prefix, ZooKeeper zookeeper, String dir) throws KeeperException, InterruptedException {
            // 获取父级ZNode下的所有节点
            List<String> names = zookeeper.getChildren(dir, false);
            for (String name : names) {
                // 如是包含此前缀的节点
                if (name.startsWith(prefix)) {
                    id = name;
                    LOG.debug("Found id created last time: {}", id);
                    break;
                }
            }
            // 无更早即顺序号更小d节点，则根据prefix创建当前节点
            if (id == null) {
                id = zookeeper.create(dir + "/" + prefix, data, getAcl(), EPHEMERAL_SEQUENTIAL);
                LOG.debug("Created id: {}", id);
            }
        }

        /**
         * 为实际获取锁而运行并重试的命令。
         *
         * @return if the command was successful or not
         */
//        @SuppressFBWarnings(
//            value = "NP_NULL_PARAM_DEREF_NONVIRTUAL",
//            justification = "findPrefixInChildren will assign a value to this.id")
        public boolean execute() throws KeeperException, InterruptedException {
            do {
                if (id == null) {
                    // 获取客户端和服务端的会话id
                    long sessionId = zookeeper.getSessionId();
                    // 生成ZNode子节点的前缀
                    String prefix = "x-" + sessionId + "-";
                    // 查找是否创建了earlier，如果没有创建该prefix节点
                    findPrefixInChildren(prefix, zookeeper, dir);
                    idName = new ZNodeName(id); // 实例化ZNode为当前id
                }
                // 获取指定ZNode下的所有子节点
                List<String> names = zookeeper.getChildren(dir, false);
                if (names.isEmpty()) {
                    LOG.warn("No children in: {} when we've just created one! Lets recreate it...", dir);
                    // lets force the recreation of the id
                    id = null;
                } else {
                    // lets sort them explicitly (though they do seem to come back in order ususally :)
                    // 子节点集合不为空，对其进行排序
                    SortedSet<ZNodeName> sortedNames = new TreeSet<>();
                    for (String name : names) {
                        sortedNames.add(new ZNodeName(dir + "/" + name));
                    }
                    // 当前锁的持有者节点，即排序后的第一个节点（id最小的最早的）
                    ownerId = sortedNames.first().getName();
                    // 返回小于idName的节点名
                    SortedSet<ZNodeName> lessThanMe = sortedNames.headSet(idName);
                    // 查看是否有比当前子节点更小（更早）的节点，如没有就可以获得锁。如果有就watch当前节点前面的一个节点
                    // 有更早的节点，说明锁已经被持有，则watch当前节点前面的一个节点
                    if (!lessThanMe.isEmpty()) {
                        // 返回当前在这个集合中的最后一个(最高的)元素。即当前节点的上一个节点
                        // 只有等上一个节点持有锁释放后，当前节点才能获取锁
                        ZNodeName lastChildName = lessThanMe.last();
                        lastChildId = lastChildName.getName();
                        LOG.debug("Watching less than me node: {}", lastChildId);
                        Stat stat = zookeeper.exists(lastChildId, new LockWatcher());
                        if (stat != null) { // 上个节点依然存在，当前节点加锁失败
                            return Boolean.FALSE;
                        } else {
                            LOG.warn("Could not find the stats for less than me: {}", lastChildName.getName());
                        }
                    } else {
                        // 如当前节点为最小（最早的）节点，成为锁的持有者
                        if (isOwner()) { // 当前节点就是持有锁的节点
                            LockListener lockListener = getLockListener();
                            if (lockListener != null) {
                                // 加锁
                                lockListener.lockAcquired();
                            }
                            return Boolean.TRUE;
                        }
                    }
                }
            } while (id == null); // 它先执行循环中知的语句,然后再判断表达式是否为真。即只要id为空，就不停的去执行加锁操作
            return Boolean.FALSE;
        }
    }

    /**
     * 加锁：
     * 尝试获取排他写锁，不管是否已获取它。
     * 请注意，由于当前锁所有者离开，可能在调用此方法之后的一段时间内获得独占锁。
     */
    public synchronized boolean lock() throws KeeperException, InterruptedException {
        if (isClosed()) { // 连接关闭返回加锁失败
            return false;
        }
        // 保证lock的ZNode是存在的，不存在会创建
        ensurePathExists(dir);
        // 执行加锁操作即LockZooKeeperOperation中的execute()方法内容，如果连接失败则延时重试。
        return (Boolean) retryOperation(zop);
    }

    /**
     * return the parent dir for lock.
     *
     * @return the parent dir used for locks.
     */
    public String getDir() {
        return dir;
    }

    /**
     * 如果此节点是锁的所有者(或领导者)，则返回true。
     */
    public boolean isOwner() {
        return id != null && id.equals(ownerId);
    }

    /**
     * 返回此锁的id。
     *
     * @return the id for this lock
     */
    public String getId() {
        return this.id;
    }

}

