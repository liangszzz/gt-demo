package com.ls.demo.gt.common.aspect;

import com.ls.demo.gt.common.response.Code;
import com.ls.demo.gt.common.response.Response;
import com.ls.demo.gt.common.transaction.GlobalTransaction;
import com.ls.demo.gt.common.transaction.GlobalTransactionManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class GlobalTransactionAspect implements Ordered {

  @Autowired private GlobalTransactionManager globalTransactionManager;

  @Around("@annotation(com.ls.demo.gt.common.transaction.GlobalTransaction)")
  public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    GlobalTransaction annotation = method.getAnnotation(GlobalTransaction.class);
    if (annotation.start()) {
      globalTransactionManager.createTransactionGroup();
    }
    try {
      globalTransactionManager.createChildTransaction();
      return joinPoint.proceed();
    } catch (Exception e) {
      log.error("GlobalTransactionAspect exception:", e);
      globalTransactionManager.rollbackCurrent();
      return Response.of(Code.ERROR);
    } finally {
      if (annotation.start()) {
        globalTransactionManager.checkAndCommitOrRollback();
      }
    }
  }

  @Override
  public int getOrder() {
    return 10000;
  }
}
