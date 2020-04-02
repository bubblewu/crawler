package com.bubble.crawler.job;

import com.bubble.crawler.bean.VideoBean;
import com.bubble.crawler.service.impl.ConsoleStoreServiceImpl;
import com.bubble.crawler.service.impl.HttpClientDownLoadServiceImpl;
import com.bubble.crawler.service.impl.QueueRepositoryServiceImpl;
import com.bubble.crawler.service.impl.YouKuProcessServiceImpl;
import com.bubble.crawler.util.ToolKits;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
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
        watcher();
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

    private static void watcher() {
        // 重试策略：重试3次，每次间隔时间指数增长(有具体增长公式)
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        // 服务器列表，格式host1:port1,host2:port2,...
        String hosts = ToolKits.getConfig("ZookeeperHost");
        // 创建客户端
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(hosts)
                .sessionTimeoutMs(5000) // 会话超时时间，单位毫秒，默认60000ms
                .connectionTimeoutMs(5000) // 连接创建超时时间，单位毫秒，默认60000ms
                .retryPolicy(retryPolicy) // 重试策略,内建有四种重试策略,也可以自行实现RetryPolicy接口
                .namespace("crawler") // 为每个业务分配一个独立的命名空间(即指定一个Zookeeper的根路径)
                .build();
        // 启动客户端
        client.start();

        try {
            //获取本地ip地址
            InetAddress localHost = InetAddress.getLocalHost();
            String ip = localHost.getHostAddress();
            // 每启动一个爬虫应用，创建一个临时节点，子节点名称为当前ip
            // 创建数据节点
            String path = ToolKits.getConfig("Path");
            client.create()
                    .creatingParentsIfNeeded() // 自动递归创建所有所需的父节点
                    // Zookeeper的节点创建模式：
                    // - PERSISTENT：持久化
                    // - PERSISTENT_SEQUENTIAL：持久化并且带序列号
                    // - EPHEMERAL：临时
                    // - EPHEMERAL_SEQUENTIAL：临时并且带序列号
                    // 如果没有设置节点属性，节点创建模式默认为持久化节点，内容默认为空
                    /*
                      ZNode主要有2种：短暂的（EPHEMERAL）和持久化的（PERSISTENT）
                      - 短暂的在客户端会话结束时，zk会将该ZNode删除；
                      - 持久化的不依赖于客户端会话，只有在客户端明确要删除的时候zk才会将该znode删除；
                    */
                    .withMode(CreateMode.EPHEMERAL) // 指定创建模式（临时节点）初始内容为空
                    // ZooDefs.Ids.OPEN_ACL_UNSAFE 默认匿名权限，权限scheme id：'world,'anyone，权限位：31（adcwr）
                    // ZooDefs.Ids.READ_ACL_UNSAFE 只读权限，权限scheme id：'world,'anyone，权限位：1（r）
                    .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE) // acl权限
                    .forPath(path + "/" + ip);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
