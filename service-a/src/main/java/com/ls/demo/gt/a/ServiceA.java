package com.ls.demo.gt.a;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "com.ls.demo.gt")
@SpringBootApplication
public class ServiceA {

  public static void main(String[] args) {
    SpringApplication.run(ServiceA.class, args);
  }
}
