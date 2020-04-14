package com.bubble.crawler.job.zk.lock;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁测试
 *
 * @author wugang
 * date: 2020-04-14 16:28
 **/
public class WriteLockTest {
    private final String servers = "localhost:2181";
    private final int sessionTimeout = 60 * 1000; // 60s
    private ZooKeeper zkClient;

//    @Before
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

    private String dir = "/" + getClass().getName();
    private WriteLock[] nodes;
    private CountDownLatch latch = new CountDownLatch(1);
    private boolean restartServer = true;
    private boolean workAroundClosingLastZNodeFails = true;
    private boolean killLeader = true;

    @Test
    public void testRun() throws Exception {
        runTest(3);
    }

    class LockCallback implements LockListener {
        public void lockAcquired() {
            latch.countDown();
        }

        public void lockReleased() {
        }
    }

    private void runTest(int count) throws Exception {
        startServer();

        nodes = new WriteLock[count];
        for (int i = 0; i < count; i++) {
            WriteLock leader = new WriteLock(zkClient, dir, null);
            nodes[i] = leader;

            leader.lock();
        }

        // 等待老的leader死亡，新节点成为leader
        latch.await(3, TimeUnit.SECONDS);
        WriteLock first = nodes[0];
        dumpNodes(count);

        // lets assert that the first election is the leader
        Assert.assertTrue("The first ZNode should be the leader " + first.getId(), first.isOwner());
        for (int i = 1; i < count; i++) {
            WriteLock node = nodes[i];
            Assert.assertFalse("Node should not be the leader " + node.getId(), node.isOwner());
        }

        if (count > 1) {
            if (killLeader) {
                System.out.println("Now killing the leader");
                // now lets kill the leader
                latch = new CountDownLatch(1);
                first.unlock();
                latch.await(30, TimeUnit.SECONDS);
                //Thread.sleep(10000);
                WriteLock second = nodes[1];
                dumpNodes(count);
                // lets assert that the first election is the leader
                Assert.assertTrue("The second znode should be the leader " + second.getId(), second.isOwner());

                for (int i = 2; i < count; i++) {
                    WriteLock node = nodes[i];
                    Assert.assertFalse("Node should not be the leader " + node.getId(), node.isOwner());
                }
            }

//            if (restartServer) {
//                // now lets stop the server
//                System.out.println("Now stopping the server");
//                stopServer();
//                Thread.sleep(10000);
//
//                // TODO lets assert that we are no longer the leader
//                dumpNodes(count);
//
//                System.out.println("Starting the server");
//                startServer();
//                Thread.sleep(10000);
//
//                for (int i = 0; i < count - 1; i++) {
//                    System.out.println("Calling acquire for node: " + i);
//                    nodes[i].lock();
//                }
//                dumpNodes(count);
//                System.out.println("Now closing down...");
//            }
        }

    }

    private void startServer() throws IOException {
        init();
    }

    private void stopServer() {
        try {
            zkClient.close();
            zkClient = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void dumpNodes(int count) {
        for (int i = 0; i < count; i++) {
            WriteLock node = nodes[i];
            System.out.println("node: " + i + " id: " + node.getId() + " is leader: " + node.isOwner());
        }
    }

//    @After
//    public void tearDown() throws Exception {
//        if (nodes != null) {
//            for (int i = 0; i < nodes.length; i++) {
//                WriteLock node = nodes[i];
//                if (node != null) {
//                    System.out.println("Closing node: " + i);
//                    node.close();
//                    if (workAroundClosingLastZNodeFails && i == nodes.length - 1) {
//                        System.out.println("Not closing zookeeper: " + i + " due to bug!");
//                    } else {
//                        System.out.println("Closing zookeeper: " + i);
//                        node.getZookeeper().close();
//                        System.out.println("Closed zookeeper: " + i);
//                    }
//                }
//            }
//        }
//        System.out.println("Now lets stop the server");
//    }

}
