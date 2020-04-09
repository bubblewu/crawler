package com.bubble.crawler.job.zk;

import com.google.common.collect.Lists;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.List;

/**
 * 分布式服务的客户端
 *
 * @author wugang
 * date: 2020-04-09 16:38
 **/
public class DistributeClient {

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        DistributeClient client = new DistributeClient();
        // 1 获取zookeeper集群连接
        client.getConnect();
        // 2 注册监听
        client.getChlidren();
        // 3 业务逻辑处理
        client.business();
    }

    private void business() throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
    }

    private void getChlidren() throws KeeperException, InterruptedException {
        List<String> children = zkClient.getChildren("/servers", true);
        // 存储服务器节点主机名称集合
        List<String> hosts = Lists.newArrayList();
        for (String child : children) {
            byte[] data = zkClient.getData("/servers/" + child, false, null);
            hosts.add(new String(data));
        }
        // 将所有在线主机名称打印到控制台
        System.out.println(hosts);
    }

    // "hadoop102:2181,hadoop103:2181,hadoop104:2181"
    private final String servers = "localhost:2181";
    private final int sessionTimeout = 2000;
    private ZooKeeper zkClient;

    private void getConnect() throws IOException {
        zkClient = new ZooKeeper(servers, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                // 收到事件通知后的回调函数（用户的业务逻辑）
                System.out.println(watchedEvent.getType() + "--" + watchedEvent.getPath());
                // 再次启动监听
                try {
                    zkClient.getChildren("/", true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
