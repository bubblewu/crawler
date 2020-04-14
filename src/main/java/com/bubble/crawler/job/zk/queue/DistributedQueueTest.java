package com.bubble.crawler.job.zk.queue;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * 基于zk实现的分布式队列测试：
 *
 * @author wugang
 * date: 2020-04-14 10:35
 **/
public class DistributedQueueTest {

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
    public void testOfferAndRemove() throws Exception {
        String dir = "/testOffer1";
        String testString = "1. Hello World";
        String testString2 = "2. Hello zk";
        DistributedQueue distributedQueue = new DistributedQueue(zkClient, dir, null);
        distributedQueue.offer(testString.getBytes());
        distributedQueue.offer(testString2.getBytes());
        byte[] dequeuedBytes = distributedQueue.remove();
        Assert.assertEquals(new String(dequeuedBytes), testString);
        System.out.println(new String(dequeuedBytes));
    }

}
