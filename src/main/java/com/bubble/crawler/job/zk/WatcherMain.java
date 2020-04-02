package com.bubble.crawler.job.zk;

import com.bubble.crawler.util.EmailUtil;
import com.bubble.crawler.util.ToolKits;
import com.google.common.collect.Lists;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 监视器
 *
 * @author wugang
 * date: 2020-04-02 17:17
 **/
public class WatcherMain implements Watcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(WatcherMain.class);
    private CuratorFramework client;
    private List<String> oldChildrenList = Lists.newArrayList();
    private final String PATH = ToolKits.getConfig("Path");

    public WatcherMain() {
        // 重试策略：重试3次，每次间隔时间指数增长(有具体增长公式)
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        // 服务器列表，格式host1:port1,host2:port2,...
        String hosts = ToolKits.getConfig("ZookeeperHost");
        // 创建客户端
        client = CuratorFrameworkFactory.builder()
                .connectString(hosts)
                .sessionTimeoutMs(5000) // 会话超时时间，单位毫秒，默认60000ms
                .connectionTimeoutMs(5000) // 连接创建超时时间，单位毫秒，默认60000ms
                .retryPolicy(retryPolicy) // 重试策略,内建有四种重试策略,也可以自行实现RetryPolicy接口
                .namespace("crawler") // 为每个业务分配一个独立的命名空间(即指定一个Zookeeper的根路径)
                .build();
        // 启动客户端
        client.start();

        // 获取子节点集合
        try {
            oldChildrenList = client.getChildren().usingWatcher(this)
                    .forPath(PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 监控节点变化
     *
     * @param watchedEvent 监控事件
     */
    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
            // 调用具体业务代码
            monitor();
        }
    }

    private void monitor() {
        try {
            List<String> currentChildrenList = client.getChildren().usingWatcher(this).forPath(PATH);
            for (String child : currentChildrenList) {
                if (!oldChildrenList.contains(child)) {
                    LOGGER.info("新增加的爬虫节点为：{}", child);
                }
            }
            for (String child : oldChildrenList) {
                if (!currentChildrenList.contains(child)) {
                    LOGGER.info("挂掉的爬虫节点为：{}", child);
                    String subject = "爬虫项目执行异常提醒";
                    String message = "ip为" + child + "服务器上的爬虫项目执行异常，请及时处理！！！";
                    EmailUtil.sendEmail(subject, message);
                }
            }
            // 子节点集合更新
            this.oldChildrenList = currentChildrenList;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        WatcherMain configWatcher = new WatcherMain();
        configWatcher.monitor();
        Thread.sleep(Long.MAX_VALUE);//然后一直监控
    }

}
