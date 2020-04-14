package com.bubble.crawler.job.zk.lock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 提供zk重试的逻辑：
 * <p>
 * A base class for protocol implementations which provides a number of higher
 * level helper methods for working with ZooKeeper along with retrying synchronous
 * operations if the connection to ZooKeeper closes such as
 * {@link #retryOperation(ZooKeeperOperation)}.
 */
class ProtocolSupport {
    private static final Logger LOG = LoggerFactory.getLogger(ProtocolSupport.class);
    private static final int RETRY_COUNT = 10; // 重试次数

    protected final ZooKeeper zookeeper;
    private AtomicBoolean closed = new AtomicBoolean(false);
    private long retryDelay = 500L;
    private List<ACL> acl = ZooDefs.Ids.OPEN_ACL_UNSAFE;

    public ProtocolSupport(ZooKeeper zookeeper) {
        this.zookeeper = zookeeper;
    }

    /**
     * Closes this strategy and releases any ZooKeeper resources; but keeps the
     * ZooKeeper instance open.
     */
    public void close() {
        if (closed.compareAndSet(false, true)) {
            doClose();
        }
    }

    /**
     * return zookeeper client instance.
     *
     * @return zookeeper client instance
     */
    public ZooKeeper getZookeeper() {
        return zookeeper;
    }

    /**
     * return the acl its using.
     *
     * @return the acl.
     */
    public List<ACL> getAcl() {
        return acl;
    }

    /**
     * set the acl.
     *
     * @param acl the acl to set to
     */
    public void setAcl(List<ACL> acl) {
        this.acl = acl;
    }

    /**
     * get the retry delay in milliseconds.
     *
     * @return the retry delay
     */
    public long getRetryDelay() {
        return retryDelay;
    }

    /**
     * Sets the time waited between retry delays.
     *
     * @param retryDelay the retry delay
     */
    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }

    /**
     * Allow derived classes to perform
     * some custom closing operations to release resources.
     */
    protected void doClose() {

    }

    /**
     * 执行给定的操作，如果连接失败则重试。
     *
     * @return object. it needs to be cast to the callee's expected return type.
     */
    protected Object retryOperation(ZooKeeperOperation operation) throws KeeperException, InterruptedException {
        KeeperException exception = null;
        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                // 执行给定的操作
                return operation.execute();
            } catch (KeeperException.SessionExpiredException e) {
                LOG.warn("Session expired {}. Reconnecting...", zookeeper, e);
                throw e;
            } catch (KeeperException.ConnectionLossException e) {
                if (exception == null) {
                    exception = e;
                }
                LOG.debug("Attempt {} failed with connection loss. Reconnecting...", i);
                retryDelay(i);
            }
        }

        throw exception;
    }

    /**
     * 确保给定的路径不存在任何数据、当前ACL和任何标志。
     *
     * @param path 路径
     */
    protected void ensurePathExists(String path) {
        ensureExists(path, null, acl, CreateMode.PERSISTENT);
    }

    /**
     * 确保给定的路径与给定的数据、ACL和标志一起存在。
     *
     * @param path
     * @param acl
     * @param flags
     */
    protected void ensureExists(final String path, final byte[] data, final List<ACL> acl, final CreateMode flags) {
        try {
            // 执行给定的操作，如果连接失败则重试
            retryOperation(() -> {
                // 获取当前路径的Stat结构体
                Stat stat = zookeeper.exists(path, false);
                // 当前路径存在返回true
                if (stat != null) {
                    return true;
                }
                // 不存在则创建，返回true
                zookeeper.create(path, data, acl, flags);
                return true;
            });
        } catch (KeeperException | InterruptedException e) {
            LOG.warn("Unexpected exception", e);
        }
    }

    /**
     * Returns true if this protocol has been closed.
     *
     * @return true if this protocol is closed
     */
    protected boolean isClosed() {
        return closed.get();
    }

    /**
     * 如果这不是第一次尝试，则执行重试延迟。
     *
     * @param attemptCount 到目前为止执行的尝试次数
     */
    protected void retryDelay(int attemptCount) {
        if (attemptCount > 0) {
            try {
                Thread.sleep(attemptCount * retryDelay);
            } catch (InterruptedException e) {
                LOG.warn("Failed to sleep.", e);
            }
        }
    }

}
