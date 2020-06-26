package com.ls.demo.gt.b;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "com.ls.demo.gt")
@SpringBootApplication
public class ServiceB {

  public static void main(String[] args) {
    SpringApplication.run(ServiceB.class, args);
  }
}
