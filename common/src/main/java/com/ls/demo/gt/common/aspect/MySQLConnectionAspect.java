package com.ls.demo.gt.common.aspect;

import com.ls.demo.gt.common.connection.MySQLConnection;
import com.ls.demo.gt.common.transaction.GlobalTransactionManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;

@Slf4j
@Aspect
@Component
public class MySQLConnectionAspect {

  @Autowired private CuratorFramework curatorFramework;

  @Around("execution(* javax.sql.DataSource.getConnection(..))")
  public Connection invoke(ProceedingJoinPoint joinPoint) throws Throwable {
    Connection connection = (Connection) joinPoint.proceed();
    if (GlobalTransactionManager.hasGroup()) {
      connection.setAutoCommit(false);
    }
    return new MySQLConnection(connection, curatorFramework);
  }
}
