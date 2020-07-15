package com.ls.demo.gt.b.service;

import com.ls.demo.gt.b.dao.TableBDao;
import com.ls.demo.gt.b.entity.TableB;
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
    private TableBDao tableBDao;

    @Autowired
    private RestTemplate restTemplate;

    @GlobalTransaction
    @Transactional
    public void insert() {
        TableB b = new TableB();
        b.setF1("tb");
        tableBDao.save(b);
    }

    @GlobalTransaction
    @Transactional
    public void insert2() {

        String response2 = restTemplate.getForObject("http://localhost:8092/", String.class);
        log.info(response2);

        TableB b = new TableB();
        b.setF1("tb");
        tableBDao.save(b);

    }
}
