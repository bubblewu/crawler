package com.bubble.crawler.service.impl;

import com.bubble.crawler.bean.VideoBean;
import com.bubble.crawler.service.StoreService;

/**
 * 控制台输出
 *
 * @author wugang
 * date: 2020-03-31 21:06
 **/
public class ConsoleStoreServiceImpl implements StoreService {

    @Override
    public void store(VideoBean video) {
//        System.out.println("name: " + video.getName());
//        System.out.println("score: " + video.getScore());
        System.out.println(video.toString());
    }
}
