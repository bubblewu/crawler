package com.bubble.crawler.job;

import com.bubble.crawler.bean.VideoBean;
import com.bubble.crawler.service.DownLoadService;
import com.bubble.crawler.service.ProcessService;
import com.bubble.crawler.service.RepositoryService;
import com.bubble.crawler.service.StoreService;

/**
 * 视频爬虫
 *
 * @author wugang
 * date: 2020-03-31 20:56
 **/
public class VideoJob {

    private DownLoadService downLoadService;
    private ProcessService processService;
    private StoreService storeService;
    private RepositoryService repositoryService;

    /**
     * 下载页面
     *
     * @param url 地址
     * @return 装载Video实体的content
     */
    public VideoBean downloadPage(String url) {
        return this.downLoadService.download(url);
    }

    /**
     * 页面解析
     *
     * @param video Video实体
     */
    public void processPage(VideoBean video) {
        this.processService.process(video);
    }

    /**
     * 存储页面信息
     *
     * @param video Video实体
     */
    public void storeVideoInfo(VideoBean video) {
        this.storeService.store(video);
    }


    public DownLoadService getDownLoadService() {
        return downLoadService;
    }

    public void setDownLoadService(DownLoadService downLoadService) {
        this.downLoadService = downLoadService;
    }

    public ProcessService getProcessService() {
        return processService;
    }

    public void setProcessService(ProcessService processService) {
        this.processService = processService;
    }

    public StoreService getStoreService() {
        return storeService;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public RepositoryService getRepositoryService() {
        return repositoryService;
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }
}
