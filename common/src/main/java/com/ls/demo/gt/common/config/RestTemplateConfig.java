package com.ls.demo.gt.common.config;

import com.ls.demo.gt.common.transaction.GlobalTransactionManager;
import com.ls.demo.gt.common.util.TransactionConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.ClientHttpRequestInitializer;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
public class RestTemplateConfig {

  @Bean
  public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getClientHttpRequestInitializers().add(clientHttpRequestInitializer());
    return restTemplate;
  }

  @Bean
  public ClientHttpRequestInitializer clientHttpRequestInitializer() {
    return request -> {
      log.info(Thread.currentThread().getName());
      if (GlobalTransactionManager.hasGroup()) {
        String id = GlobalTransactionManager.globalTransactionId.get();
        if (!StringUtils.isEmpty(id))
          request.getHeaders().add(TransactionConstant.TRANSACTION_HEADER_NAME, id);
      }
    };
  }
}
