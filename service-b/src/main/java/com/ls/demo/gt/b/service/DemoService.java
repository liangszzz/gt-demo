package com.ls.demo.gt.b.service;

import com.ls.demo.gt.b.dao.TableBDao;
import com.ls.demo.gt.b.entity.TableB;
import com.ls.demo.gt.common.transaction.GlobalTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoService {
  @Autowired private TableBDao tableBDao;

  @GlobalTransaction
  @Transactional
  public void insert() {
    TableB b = new TableB();
    b.setF1("t2");
    tableBDao.save(b);
  }
}
