package com.bubble.crawler.service.impl;

import com.bubble.crawler.service.RepositoryService;
import com.bubble.crawler.util.ToolKits;
import com.bubble.crawler.util.db.RedisUtil;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Random;

/**
 * 基于Redis List列表的url仓库实现类：随机取不同视频网站url，降低单个网站频繁访问
 *
 * @author wugang
 * date: 2020-04-02 11:19
 **/
public class RandomRedisRepositoryServiceImpl implements RepositoryService {
    // 顶级域名+redisKey
    private HashMap<String, String> hashMap = Maps.newHashMap();
    private Random random = new Random();
    private RedisUtil redisUtil = new RedisUtil();

    @Override
    public String poll() {
        String[] keyArr = hashMap.keySet().toArray(new String[0]);
        int nextInt = random.nextInt(keyArr.length);
        String key = keyArr[nextInt];
        String value = hashMap.get(key);
        return redisUtil.poll(value);
    }

    @Override
    public void addHighLevel(String url) {
        // 获取顶级域名
        String topDomain = ToolKits.getTopDomain(url);
        // 根据顶级域名获取redis key
//        String redisKey = hashMap.get(topDomain);
//        if (redisKey == null) {
//            redisKey = topDomain;
//            hashMap.put(topDomain, redisKey);
//        }
        // 若topDomain对应的value为空，会将第二个参数的返回值存入并返回
//        Map<String, String> map = Maps.newHashMap();
//        map.put("youku", "youku.com");
//        System.out.println(map.computeIfAbsent("youku", d -> d)); // youku.com
//        System.out.println(map.computeIfAbsent("tencent", d -> d)); // tencent
//        System.out.println(map.computeIfAbsent("iqiyi", d -> "iqiyi.com")); // iqiyi.com
        hashMap.computeIfAbsent(topDomain, d -> topDomain);
        redisUtil.add(topDomain, url);
    }

    @Override
    public void addLowLevel(String url) {
        this.addHighLevel(url);
    }

}
