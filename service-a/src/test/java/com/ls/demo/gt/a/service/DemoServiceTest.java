package com.ls.demo.gt.a.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.Table;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DemoServiceTest {

  @Autowired private DemoService demoService;

  @Test
  void test_insert_success() {
    demoService.insert();
  }
}
