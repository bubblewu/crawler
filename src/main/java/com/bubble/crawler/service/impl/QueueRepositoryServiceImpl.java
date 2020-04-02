package com.bubble.crawler.service.impl;

import com.bubble.crawler.service.RepositoryService;
import com.google.common.collect.Queues;
import org.apache.commons.lang.StringUtils;

import java.util.Queue;

/**
 * 基于Queue的url仓库实现类
 *
 * @author wugang
 * date: 2020-04-02 11:06
 **/
public class QueueRepositoryServiceImpl implements RepositoryService {
    //高优先级
    private Queue<String> highLevelQueue = Queues.newConcurrentLinkedQueue();
    //低优先级
    private Queue<String> lowLevelQueue = Queues.newConcurrentLinkedQueue();

    @Override
    public String poll() {
        // 先解析高优先级队列
        String url = highLevelQueue.poll();
        if (StringUtils.isBlank(url)) {
            // 然后在解析低优先级队列
            url = lowLevelQueue.poll();
        }
        return url;
    }

    @Override
    public void addHighLevel(String url) {
        this.highLevelQueue.add(url);
    }

    @Override
    public void addLowLevel(String url) {
        this.lowLevelQueue.add(url);
    }
}
