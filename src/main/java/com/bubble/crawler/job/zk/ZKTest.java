package com.bubble.crawler.job.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Zookeeper使用测试
 *
 * @author wugang
 * date: 2020-04-09 14:22
 **/
public class ZKTest {
    // "hadoop102:2181,hadoop103:2181,hadoop104:2181"
    private final String servers = "localhost:2181";
    private final int sessionTimeout = 2000;
    private ZooKeeper zkClient;

    @Before
    public void init() throws IOException {
        zkClient = new ZooKeeper(servers, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                // 收到事件通知后的回调函数（用户的业务逻辑）
                System.out.println(watchedEvent.getType() + "--" + watchedEvent.getPath());
                // 再次启动监听
                try {
                    zkClient.getChildren("/", true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Test
    public void create() throws KeeperException, InterruptedException {
        // 参数1：要创建的节点的路径；
        // 参数2：节点数据；
        // 参数3：节点权限；
        // 参数4：节点的类型：临时/持久
        String path = zkClient.create("/test", "helloZK".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT // CreateMode.EPHEMERAL
        );
        System.out.println(path);
        Thread.sleep(Long.MAX_VALUE);
        /*
        [zk: localhost:2181(CONNECTED) 8] ls /
        [test, zookeeper]
        [zk: localhost:2181(CONNECTED) 9] ls /test
        []
        [zk: localhost:2181(CONNECTED) 11] get /test
        helloZK
        * */
    }

    @Test
    public void getDataAndWatch() throws KeeperException, InterruptedException {
        // 获取子节点 并监控节点的变化
        List<String> children = zkClient.getChildren("/", true);

        for (String child : children) {
            System.out.println(child);
        }
        // 延时阻塞
        Thread.sleep(Long.MAX_VALUE);
        /*
        None--null
        zookeeper
        test
        -- 删除test目录：
        NodeChildrenChanged--/
        * */
    }

    @Test
    public void exist() throws Exception {
        // 判断znode是否存在
        Stat stat = zkClient.exists("/test", false);
        System.out.println(stat == null ? "not exist" : "exist");
    }

    // 基于Curator的分布式锁实现
    public void distributeLock() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(
                servers,
                new RetryNTimes(10, 5000)
        );
        InterProcessMutex lock = new InterProcessMutex(client, "/locks/my_lock");
        lock.acquire(); // 加锁
        // 业务 ...
        lock.release(); // 释放锁
    }

}
