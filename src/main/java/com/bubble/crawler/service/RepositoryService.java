package com.bubble.crawler.service;

/**
 * URL存储仓库（存储队列）
 *
 * @author wugang
 * date: 2020-04-02 11:05
 **/
public interface RepositoryService {

    /**
     * 弹出一个url
     *
     * @return url
     */
    String poll();

    /**
     * 高优先级
     *
     * @param url url地址
     */
    void addHighLevel(String url);

    /**
     * 低优先级
     *
     * @param url url地址
     */
    void addLowLevel(String url);
}
