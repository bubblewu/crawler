package com.bubble.crawler.job.zk.lock;

import org.apache.zookeeper.KeeperException;

/**
 * 用来执行重试zookeeper命令
 * A callback object which can be used for implementing retry-able operations in the
 * {@link ProtocolSupport} class.
 *
 */
public interface ZooKeeperOperation {

    /**
     * Performs the operation - which may be involved multiple times if the connection
     * to ZooKeeper closes during this operation.
     *
     * @return the result of the operation or null
     * @throws KeeperException
     * @throws InterruptedException
     */
    boolean execute() throws KeeperException, InterruptedException;

}
