package com.ls.demo.gt.common.config;

import com.ls.demo.gt.common.util.TransactionConstant;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@EnableConfigurationProperties(ZookeeperProperties.class)
public class ZookeeperConfig {

    @Autowired
    private ZookeeperProperties zookeeperProperties;

    @Scope(value = "prototype")
    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework curatorFramework() {
        return CuratorFrameworkFactory.newClient(
                zookeeperProperties.getConnectString(),
                zookeeperProperties.getSessionTimeoutMs(),
                zookeeperProperties.getConnectionTimeoutMs(),
                new RetryNTimes(zookeeperProperties.getRetryTimes(), zookeeperProperties.getSleepMsBetweenRetries()));
    }
}
