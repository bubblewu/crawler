package com.bubble.crawler.service.impl;

import com.bubble.crawler.bean.VideoBean;
import com.bubble.crawler.service.DownLoadService;
import com.bubble.crawler.util.PageDownLoadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HttpClient下载
 *
 * @author wugang
 * date: 2020-03-31 20:38
 **/
public class HttpClientDownLoadServiceImpl implements DownLoadService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientDownLoadServiceImpl.class);

    @Override
    public VideoBean download(String url) {
        LOGGER.info("start download: {}", url);
        VideoBean video = new VideoBean();
        video.setUrl(url);
        video.setContent(PageDownLoadUtil.getPageContent(url));
        return video;
    }
}
