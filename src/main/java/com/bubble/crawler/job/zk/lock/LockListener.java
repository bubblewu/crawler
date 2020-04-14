package com.bubble.crawler.job.zk.lock;

/**
 * This class has two methods which are call
 * back methods when a lock is acquired and
 * when the lock is released.
 */
public interface LockListener {

    /**
     * 获得锁
     * call back called when the lock is acquired.
     */
    void lockAcquired();

    /**
     * 锁释放
     * call back called when the lock is released.
     */
    void lockReleased();

}
