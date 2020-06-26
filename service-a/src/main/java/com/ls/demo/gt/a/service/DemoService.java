package com.ls.demo.gt.a.service;

import com.ls.demo.gt.a.dao.TableADao;
import com.ls.demo.gt.a.entity.TableA;
import com.ls.demo.gt.common.transaction.GlobalTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class DemoService {

  @Autowired private RestTemplate restTemplate;

  @Autowired private TableADao tableADao;

  @GlobalTransaction(start = true)
  @Transactional
  public void insert() {

    String response = restTemplate.getForObject("http://localhost:8091/", String.class);
    log.info(response);

    TableA a = new TableA();
    a.setF1("t1");
    tableADao.save(a);
  }

  @GlobalTransaction(start = true)
  @Transactional
  public void insertFail() {
    String response = restTemplate.getForObject("http://localhost:8091/", String.class);
    log.info(response);

    TableA a = new TableA();
    a.setF1("t1");
    tableADao.save(a);
    throw new RuntimeException("test rollback");
  }
}
