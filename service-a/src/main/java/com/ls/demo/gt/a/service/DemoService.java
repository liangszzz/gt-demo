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

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TableADao tableADao;

    @GlobalTransaction(start = true)
    @Transactional
    public void insert() {

        String response = restTemplate.getForObject("http://localhost:8091/", String.class);
        log.info(response);
        String response2 = restTemplate.getForObject("http://localhost:8092/", String.class);
        log.info(response2);

        TableA a = new TableA();
        a.setF1("ta");
        tableADao.save(a);

    }

    @GlobalTransaction(start = true)
    public void insert2() {
        String response = restTemplate.getForObject("http://localhost:8091/success2", String.class);
        log.info(response);

        TableA a = new TableA();
        a.setF1("ta");
        tableADao.save(a);
    }

    @GlobalTransaction(start = true)
    @Transactional
    public void insert3() {
        String response = restTemplate.getForObject("http://localhost:8091/", String.class);
        log.info(response);
        String response2 = restTemplate.getForObject("http://localhost:8092/", String.class);
        log.info(response2);
        throw new RuntimeException("test rollback1");
    }

    @GlobalTransaction(start = true)
    @Transactional
    public void insert4() {

        String response = restTemplate.getForObject("http://localhost:8091/", String.class);
        log.info(response);
        String response2 = restTemplate.getForObject("http://localhost:8092/fail2", String.class);
        log.info(response2);
        TableA a = new TableA();
        a.setF1("ta");
        tableADao.save(a);
    }
}
