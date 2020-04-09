package com.bubble.crawler.job;

import com.bubble.crawler.bean.VideoBean;
import com.bubble.crawler.job.zk.RegisterZK;
import com.bubble.crawler.service.impl.ConsoleStoreServiceImpl;
import com.bubble.crawler.service.impl.HttpClientDownLoadServiceImpl;
import com.bubble.crawler.service.impl.QueueRepositoryServiceImpl;
import com.bubble.crawler.service.impl.YouKuProcessServiceImpl;
import com.bubble.crawler.util.ToolKits;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 主入口
 *
 * @author wugang
 * date: 2020-03-31 20:53
 **/
public class StartMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(StartMain.class);

    // 定长线程池
    private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(Integer.parseInt(ToolKits.getConfig("ThreadNum")));

    public static void main(String[] args) {
        // 首先，爬虫服务端向Zookeeper注册，创建临时节点 /crawler/ip
        RegisterZK.register();

        VideoJob job = new VideoJob();
        job.setDownLoadService(new HttpClientDownLoadServiceImpl());
        job.setProcessService(new YouKuProcessServiceImpl());
        job.setStoreService(new ConsoleStoreServiceImpl());
        job.setRepositoryService(new QueueRepositoryServiceImpl());

        String url = "http://www.youku.com/show_page/id_z9cd2277647d311e5b692.html?spm=a2htv.20005143.m13050845531.5~5~1~3~A&from=y1.3-tv-index-2640-5143.40177.1-1";
//        String iqiyiUrl = "http://list.iqiyi.com/www/2/-------------10-3-1---.html";
        job.getRepositoryService().addHighLevel(url);

        start(job);

    }

    private static void start(VideoJob job) {
        while (true) {
            // 从队列中提取需要解析的url
            String url = job.getRepositoryService().poll();
            //判断url是否为空
            if (StringUtils.isNotBlank(url)) {
                fixedThreadPool.execute(() -> {
                    LOGGER.info("当前线程: {}", Thread.currentThread().getName());
                    // 下载网页
                    VideoBean video = job.downloadPage(url);
                    // 解析
                    job.processPage(video);
                    List<String> urlList = video.getUrlList();
                    for (String each : urlList) {
                        if (each.startsWith("http://www.youku.com/")) {
                            job.getRepositoryService().addHighLevel(each);
                        } else {
                            job.getRepositoryService().addLowLevel(each);
                        }
                    }
                    // 存储
                    job.storeVideoInfo(video);

                    ToolKits.sleep(Long.parseLong(ToolKits.getConfig("Millions_3")));
                });
            } else {
                LOGGER.warn("队列中的url解析完毕，waiting ...");
                ToolKits.sleep(Long.parseLong(ToolKits.getConfig("Millions_5")));
            }
        }
    }

}
