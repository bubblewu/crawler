package com.bubble.crawler.service.impl;

import com.bubble.crawler.service.RepositoryService;
import com.bubble.crawler.util.ToolKits;
import com.bubble.crawler.util.db.RedisUtil;
import org.apache.commons.lang.StringUtils;

/**
 * 基于Redis List列表的url仓库实现类
 *
 * @author wugang
 * date: 2020-04-02 11:10
 **/
public class RedisRepositoryServiceImpl implements RepositoryService {
    private final RedisUtil redisUtil = new RedisUtil();
    // redis中列表key的名称
    private final String HIGH_KEY = ToolKits.getConfig("HighKey");
    private final String LOW_KEY = ToolKits.getConfig("LowKey");

    @Override
    public String poll() {
        String url = redisUtil.poll(HIGH_KEY);
        if (StringUtils.isBlank(url)) {
            url = redisUtil.poll(LOW_KEY);
        }
        return url;
    }

    @Override
    public void addHighLevel(String url) {
        this.redisUtil.add(HIGH_KEY, url);
    }

    @Override
    public void addLowLevel(String url) {
        this.redisUtil.add(LOW_KEY, url);
    }
}
