package com.ls.demo.gt.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ThreadPoolConfig {

  @Bean
  public ScheduledExecutorService scheduledPool() {
    return Executors.newScheduledThreadPool(3, Thread::new);
  }
}
