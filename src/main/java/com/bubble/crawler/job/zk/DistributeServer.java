package com.bubble.crawler.job.zk;

import org.apache.zookeeper.*;

import java.io.IOException;

/**
 * 分布式服务：服务器端向Zookeeper注册
 *
 * @author wugang
 * date: 2020-04-09 16:34
 **/
public class DistributeServer {
    // "hadoop102:2181,hadoop103:2181,hadoop104:2181"
    private final String servers = "localhost:2181";
    private final int sessionTimeout = 2000;
    private ZooKeeper zkClient;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        DistributeServer server = new DistributeServer();
        // 1 连接zookeeper集群
        server.getConnect();
        // 2 注册节点
        server.register(args[0]);
        // 3 业务逻辑处理
        server.business();
    }

    private void business() throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
    }

    private void register(String hostname) throws KeeperException, InterruptedException {
        // 在集群上创建/servers节点
        String path = zkClient.create("/servers/server", hostname.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println(hostname + " is online ");
    }

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
