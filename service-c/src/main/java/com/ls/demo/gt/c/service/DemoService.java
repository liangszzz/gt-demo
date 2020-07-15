package com.ls.demo.gt.c.service;

import com.ls.demo.gt.c.dao.TableCDao;
import com.ls.demo.gt.c.entity.TableC;
import com.ls.demo.gt.common.transaction.GlobalTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoService {
    @Autowired
    private TableCDao tableBDao;

    @GlobalTransaction
    @Transactional
    public void insert() {
        TableC t = new TableC();
        t.setF1("tc");
        tableBDao.save(t);
    }

    @GlobalTransaction
    @Transactional
    public void insert4() {
        throw new RuntimeException("test rollback2");
    }
}
