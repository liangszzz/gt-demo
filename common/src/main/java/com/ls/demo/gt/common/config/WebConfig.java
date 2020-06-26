package com.ls.demo.gt.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Slf4j
@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

  @Override
  protected void addInterceptors(InterceptorRegistry registry) {
    super.addInterceptors(registry);
    registry.addInterceptor(getGroupIdHandlerInterceptor());
  }

  @Bean
  public GroupIdHandlerInterceptor getGroupIdHandlerInterceptor() {
    return new GroupIdHandlerInterceptor();
  }
}
