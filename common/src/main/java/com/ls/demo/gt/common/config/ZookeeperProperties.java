package com.ls.demo.gt.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "zookeeper")
public class ZookeeperProperties {

    private String connectString;
    private int sessionTimeoutMs=5000;
    private int connectionTimeoutMs=6000;
    private int retryTimes=5;
    private int sleepMsBetweenRetries=5000;


}
