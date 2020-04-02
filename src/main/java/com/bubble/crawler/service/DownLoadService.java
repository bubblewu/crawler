package com.bubble.crawler.service;

import com.bubble.crawler.bean.VideoBean;

/**
 * 页面下载
 *
 * @author wugang
 * date: 2020-03-31 20:36
 **/
public interface DownLoadService {

    VideoBean download(String url);

}
