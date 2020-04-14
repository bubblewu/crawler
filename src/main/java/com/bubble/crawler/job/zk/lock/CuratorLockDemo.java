package com.bubble.crawler.job.zk.lock;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * 基于Curator框架的分布式锁案例
 *
 * @author wugang
 * date: 2020-04-14 17:25
 **/
public class CuratorLockDemo {

    public static void main(String[] args) {
        //1 重试策略：初试时间为1s 重试10次
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
        //2 通过工厂创建连接
        CuratorFramework cf = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .sessionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .build();
        //3 开启连接
        cf.start();
//        try {
//            // 异步创建
//            cf.create().withMode(CreateMode.PERSISTENT).inBackground().forPath("/test", "haha".getBytes());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // 4 锁构建
        // 传入一个根节点，在获取锁的时候，会在这个根节点下面做一些操作
        InterProcessMutex lock = new InterProcessMutex(cf, "/cur-lock");

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                System.out.println(">> " + Thread.currentThread().getName() + " 尝试获取锁 ...");
                try {
                    // 可重入
                    lock.acquire();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + "获得锁");

                try {
                    System.out.println(Thread.currentThread().getName() + "执行中 doSomething...");
                    Thread.sleep(1000 * 3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + "执行完，释放锁");
                try {
                    lock.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + "释放成功");
            }, "线程-" + i).start();
        }

    }

}
