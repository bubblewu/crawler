package com.bubble.crawler.job.zk.queue;

import org.apache.zookeeper.KeeperException;

/**
 * 队列接口
 *
 * @author wugang
 * date: 2020-04-14 10:38
 **/
public interface Queue {
    /**
     * 在不修改队列的情况下返回队列的头部。
     *
     * @return 队列头部的数据
     */
    byte[] element();

    /**
     * 删除队列头元素，并返回
     *
     * @return 队列头元素
     */
    byte[] remove();

    /**
     * 删除队列的头并返回它，阻塞直到它成功。
     *
     * @return 队列的头
     */
    byte[] take();

    /**
     * 插入数据
     *
     * @param data 待插入的数据
     * @return true if data was successfully added
     */
    boolean offer(byte[] data) throws KeeperException, InterruptedException;

    /**
     * 返回队列第一个元素的数据，如果队列为空，则返回null。
     *
     * @return 队列第一个元素的数据
     */
    byte[] peek();

    /**
     * 尝试删除队列的头并返回它。如果队列为空，则返回null。
     *
     * @return 队列头
     */
    byte[] poll();

}
