#### 基于ZK的分布式事务demo

### 理论

CAP

C(一致性), 数据一致更新，所有数据变动都是同步的

A(可用性), 好的响应性能（指系统能够很好的为用户服务，访问超时等用户体验不好的情况）

P(分区容忍性) 可靠性（遇到某节点或网络分区故障时，仍然能够对外提供满足一致性和可用性的服务。）

BASE

BASE思想主要强调基本的可用性，如果你需要High 可用性，也就是纯粹的高性能，那么就要以一致性或容忍性为牺牲，BASE思想的方案在性能上还是有潜力可挖的。、

### 常见方案

- 两阶段提交 2PC
- 基于可靠消息服务的分布式事务
- 最大努力通知（定期校对）
- TCC(try-commit-cancel)

### 公司事务

- tx-lcn(https://github.com/codingapi/tx-lcn)
- seata(https://github.com/seata/seata)

### 准备

三个不同的spring-boot 服务service-a,service-b,service-c

三个服务分别连的数据库 是gta,gtb,gtc

每个服务的功能是向表table_a,table_b,table_c插入一条数据

#### 目标

service-a调用 service-b ,然后service-a调用service-c

service-a调用 service-b,然后service-b调用service-c

- 要保证,服务没有抛出异常的情况下,三张表的数据全部插入成功
- 要保证,三个服务有抛出异常的情况下,三张表的数据全部插入失败

#### 效果

- service-a   --------->    service-b  service-a   --------->    service-c 
  - 调用接口:   http://localhost:8090/success1 
- service-a   --------->    service-b   ---------> service-c 
  - 调用接口:   http://localhost:8090/success2
- service-a  --------->    service-b    service-a   --------->    service-c     执行后service-a抛出异常
  - 调用接口:   http://localhost:8090/fail1
- service-a  --------->    service-b    service-a   --------->    service-c    service-c  抛出异常
  - 调用接口:   http://localhost:8090/fail2

#### 原理

1. 使用切面拦截 mysql的connection,替换成自己的connection

2. 使用切面拦截 自己写的 GlobalTransaction 注解

3. 保证GlobalTransaction切面的执行在transaction后

4. 执行GlobalTransaction切面时 将事务组注册到zk上去

5. 将事务组ID在微服务调用时发送给下一个服务

6. 重写mysql connection 的commit 和 rollback 方法

7. 在有事务组的情况下 commit 不提交,注册分支ZK的data为COMMOT状态,注册监听zk的事务组ID

8. 在有事务组的情况下 方法出现异常,注册分支ZK的data为ROLLBACK状态,注册监听zk的事务组ID

9. 事务发起者在切面结束后,遍历ZK,事务组ID下的节点信息,如果都为COMMIT,那么将事务组节点的data设置为COMMIT,否则为ROLLBACK

10. 各个分支在检测到事务组节点的数据 变更为 COMMIT或者ROLLBACK时 调用connection.commit() 或 connection.rollback

#### TODO

1. 事务commit前抛出异常,本节点立即回滚,其他节点不再执行

2. 解析简单的SQL语句,写入redolog,commit失败后可以回滚

#### 代码

####  https://github.com/yiyan1992/gt-demo

```java
//使用切面拦截 mysql的connection,替换成自己的connection
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
```

```java
//使用切面拦截 自己写的 GlobalTransaction 注解
//保证GlobalTransaction切面的执行在transaction后
//执行GlobalTransaction切面时 将事务组注册到zk上去
//事务发起者在切面结束后,遍历ZK,事务组ID下的节点信息,如果都为COMMIT,那么将事务组节点的data设置为COMMIT,否则为ROLLBACK
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
```

```java
//重写mysql connection 的commit 和 rollback 方法
//在有事务组的情况下 commit 不提交,注册分支ZK的data为COMMOT状态,注册监听zk的事务组ID
//在有事务组的情况下 方法出现异常,注册分支ZK的data为ROLLBACK状态,注册监听zk的事务组ID
//各个分支在检测到事务组节点的数据 变更为 COMMIT或者ROLLBACK时 调用connection.commit() 或 connection.rollback
public class MySQLConnection{
    
    private Connection connection;
    
      @Override
      public void commit() throws SQLException {
        if (!GlobalTransactionManager.hasGroup()) {
          connection.commit();
          return;
        }
    
        String childPath = GlobalTransactionManager.getChildPath();
        try {
          curatorFramework
              .setData()
              .forPath(childPath, TransactionConstant.COMMIT_VALUE.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
          log.error("commit exception", e);
          throw new RuntimeException(e);
        }
    
        String parentPath = GlobalTransactionManager.getParentPath();
        NodeCache cache = new NodeCache(curatorFramework, parentPath, false);
        try {
          cache.start();
        } catch (Exception e) {
          log.error("启动监听失败", e);
          throw new RuntimeException(e);
        }
    
        cache
            .getListenable()
            .addListener(
                new TransactionNodeCacheListener(connection) {
                  @Override
                  public void nodeChanged() throws Exception {
                    String data = new String(cache.getCurrentData().getData(), StandardCharsets.UTF_8);
                    log.info("value:{}", data);
                    switch (data) {
                      case TransactionConstant.COMMIT_VALUE:
                        try {
                          getConnection().commit();
                        } finally {
                          getConnection().close();
                          cache.getListenable().removeListener(this);
                        }
                        break;
                      case TransactionConstant.ROLLBACK_VALUE:
                        try {
                          getConnection().rollback();
                          getConnection().close();
                        } finally {
                          cache.getListenable().removeListener(this);
                        }
                        break;
                    }
                  }
                });
      }
}
```

```java

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
```
```java
//将事务组ID在微服务调用时发送给下一个服务
@Slf4j
@Configuration
public class RestTemplateConfig {

  @Bean
  public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getClientHttpRequestInitializers().add(clientHttpRequestInitializer());
    return restTemplate;
  }

  @Bean
  public ClientHttpRequestInitializer clientHttpRequestInitializer() {
    return request -> {
      log.info(Thread.currentThread().getName());
      if (GlobalTransactionManager.hasGroup()) {
        String id = GlobalTransactionManager.globalTransactionId.get();
        if (!StringUtils.isEmpty(id))
          request.getHeaders().add(TransactionConstant.TRANSACTION_HEADER_NAME, id);
      }
    };
  }
}
```
```java
//下游服务接受事务组ID
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

```