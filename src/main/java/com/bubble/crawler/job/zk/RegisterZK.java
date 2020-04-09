package com.bubble.crawler.job.zk;

import com.bubble.crawler.util.ToolKits;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

import java.net.InetAddress;

/**
 * 爬虫服务端向Zookeeper注册
 *
 * @author wugang
 * date: 2020-04-09 16:45
 **/
public class RegisterZK {

    /**
     * 爬虫服务端向Zookeeper注册，创建临时节点 /crawler/ip
     */
    public static void register() {
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

}
