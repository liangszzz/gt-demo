package com.ls.demo.gt.common.transaction;

import com.ls.demo.gt.common.util.TransactionConstant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class GlobalTransactionManager {

  private final CuratorFramework curatorFramework;

  public static ThreadLocal<String> globalTransactionId = new ThreadLocal<>();

  public static ThreadLocal<String> transactionId = new ThreadLocal<>();

  public GlobalTransactionManager(CuratorFramework curatorFramework) {
    this.curatorFramework = curatorFramework;
  }

  @Autowired private ScheduledExecutorService scheduledPool;

  public static boolean hasGroup() {
    return !StringUtils.isEmpty(GlobalTransactionManager.globalTransactionId.get());
  }

  public static String getParentPath() {
    return TransactionConstant.PARENT_PATH
        + "/"
        + GlobalTransactionManager.globalTransactionId.get();
  }

  public static String getChildPath() {
    return GlobalTransactionManager.transactionId.get();
  }

  /**
   * 创建事务组
   *
   * @throws Exception
   */
  public void createTransactionGroup() throws Exception {
    log.info(Thread.currentThread().getName());
    String s = UUID.randomUUID().toString();
    String path = TransactionConstant.PARENT_PATH + "/" + s;
    String result =
        curatorFramework
            .create()
            .withMode(CreateMode.PERSISTENT)
            .forPath(path, TransactionConstant.DEFAULT_VALUE.getBytes(StandardCharsets.UTF_8));
    log.info("{}:{}", TransactionConstant.TRANSACTION_HEADER_NAME, result);
    if (!StringUtils.isEmpty(result)) {
      GlobalTransactionManager.globalTransactionId.set(s);
    }
  }

  /** 创建分支事务 */
  public void createChildTransaction() throws Exception {

    String transactionId = UUID.randomUUID().toString();

    String childPath =
        TransactionConstant.PARENT_PATH
            + "/"
            + GlobalTransactionManager.globalTransactionId.get()
            + "/"
            + transactionId;

    String newPath =
        curatorFramework
            .create()
            .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
            .forPath(childPath, TransactionConstant.DEFAULT_VALUE.getBytes(StandardCharsets.UTF_8));

    log.error("child path :{}", newPath);
    GlobalTransactionManager.transactionId.set(newPath);
  }

  public void checkAndCommitOrRollback() throws Exception {
    if (!hasGroup()) {
      return;
    }
    String parentPath =
        TransactionConstant.PARENT_PATH + "/" + GlobalTransactionManager.globalTransactionId.get();
    List<String> strings = curatorFramework.getChildren().forPath(parentPath);
    if (strings.size() == 0) {
      return;
    }
    boolean commit = true;
    for (String str : strings) {
      String path =
          TransactionConstant.PARENT_PATH
              + "/"
              + GlobalTransactionManager.globalTransactionId.get()
              + "/"
              + str;
      String data = new String(curatorFramework.getData().forPath(path), StandardCharsets.UTF_8);
      if (!TransactionConstant.COMMIT_VALUE.equals(data)) {
        commit = false;
        break;
      }
    }
    byte[] data =
        commit
            ? TransactionConstant.COMMIT_VALUE.getBytes(StandardCharsets.UTF_8)
            : TransactionConstant.ROLLBACK_VALUE.getBytes(StandardCharsets.UTF_8);
    curatorFramework
        .setData()
        .forPath(
            TransactionConstant.PARENT_PATH
                + "/"
                + GlobalTransactionManager.globalTransactionId.get(),
            data);

    createClearTask();
  }

  public void rollbackCurrent() throws Exception {
    if (!hasGroup()) {
      return;
    }
    curatorFramework
        .setData()
        .forPath(
            GlobalTransactionManager.transactionId.get(),
            TransactionConstant.ROLLBACK_VALUE.getBytes(StandardCharsets.UTF_8));
    createClearTask();
  }

  private static void remove() {
    GlobalTransactionManager.globalTransactionId.remove();
    GlobalTransactionManager.transactionId.remove();
  }

  public void createClearTask() throws Exception {
    scheduledPool.schedule(
        new ClearRunnable(curatorFramework, GlobalTransactionManager.globalTransactionId.get()),
        5,
        TimeUnit.SECONDS);
    remove();
  }

  static class ClearRunnable implements Runnable {

    private final CuratorFramework curatorFramework;

    private final String globalTransactionId;

    public ClearRunnable(CuratorFramework curatorFramework, String globalTransactionId) {
      this.curatorFramework = curatorFramework;
      this.globalTransactionId = globalTransactionId;
    }

    @SneakyThrows
    @Override
    public void run() {
      String parentPath = TransactionConstant.PARENT_PATH + "/" + globalTransactionId;
      curatorFramework.delete().deletingChildrenIfNeeded().forPath(parentPath);
    }
  }
}
