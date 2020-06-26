package com.ls.demo.gt.common.config;

import com.ls.demo.gt.common.transaction.GlobalTransactionManager;
import com.ls.demo.gt.common.util.TransactionConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class GroupIdHandlerInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    String groupId = request.getHeader(TransactionConstant.TRANSACTION_HEADER_NAME);
    if (!StringUtils.isEmpty(groupId) && !"null".equals(groupId)) {
      GlobalTransactionManager.globalTransactionId.set(groupId);
    }
    return true;
  }
}
