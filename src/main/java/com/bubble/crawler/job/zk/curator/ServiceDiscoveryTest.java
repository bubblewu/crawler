package com.bubble.crawler.job.zk.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.junit.Assert;
import org.junit.Test;

/**
 * 基于Curator的服务发现测试
 *
 * @author wugang
 * date: 2020-04-15 15:18
 **/
public class ServiceDiscoveryTest {
    private String connectStr = "localhost:2181";

    @Test
    public void testDiscovery() throws Exception {
        String serviceName = "test";
        String basePath = "/services";
        CuratorFramework client = CuratorFrameworkFactory.newClient(connectStr, new RetryOneTime(1000));
        client.start();
        // 创建2个服务实例
        ServiceInstance<String> instance1 = ServiceInstance.<String>builder().payload("plant").name(serviceName).port(10064).build();
        ServiceInstance<String> instance2 = ServiceInstance.<String>builder().payload("animal").name(serviceName).port(10065).build();
        System.out.println("instance 1: " + instance1.getId());
        System.out.println("instance 2: " + instance2.getId());

        ServiceDiscovery<String> discovery = ServiceDiscoveryBuilder.builder(String.class)
                .basePath(basePath).client(client).thisInstance(instance1).build();
        discovery.start();
        discovery.registerService(instance2);

        ServiceProvider<String> provider = discovery.serviceProviderBuilder().serviceName(serviceName).build();
        provider.start();

        Assert.assertFalse(provider.getInstance().getId().isEmpty());
        Assert.assertTrue(provider.getAllInstances().contains(instance1) && provider.getAllInstances().contains(instance2));

        client.delete().deletingChildrenIfNeeded().forPath(basePath);

        CloseableUtils.closeQuietly(provider);
        CloseableUtils.closeQuietly(discovery);
        CloseableUtils.closeQuietly(client);
    }

}
