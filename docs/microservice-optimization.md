# 社交平台微服务优化文档

## 概述

将社交平台从"能跑的项目"改造为标准 Spring Cloud Alibaba 微服务架构，涵盖以下 5 个方面：

| 步骤 | 技术 | 目标 |
|------|------|------|
| 1 | Nacos + Gateway | 服务注册发现 + 统一网关入口 |
| 2 | RocketMQ | 异步消息解耦（发布动态→通知落库） |
| 3 | Sentinel | 接口限流熔断保护 |
| 4 | Seata | 分布式事务一致性 |
| 5 | Docker Compose | 中间件容器化编排 |

---

## 技术栈版本

| 组件 | 版本 |
|------|------|
| Spring Boot | 3.2.4 |
| Spring Cloud | 2023.0.1 |
| Spring Cloud Alibaba | 2023.0.1.0 |
| RocketMQ Starter | 2.3.0 |
| Seata Starter | 2.1.0 |
| Sentinel | 随 Spring Cloud Alibaba BOM |
| Nacos | 2.3.0 |
| RocketMQ Server | 5.1.4 |
| Seata Server | 1.7.0 |

---

## 第一步：Nacos + Gateway

### 改动内容

**Nacos 服务注册发现**：8 个后端服务全部注册到 Nacos（`localhost:8848`），每个服务的 `application.yml` 中配置了 `spring-cloud-starter-alibaba-nacos-discovery`。

**Spring Cloud Gateway**：新增 `social-gateway` 服务（端口 8080），作为统一入口，通过 `lb://` 负载均衡路由到各微服务。

### Gateway 路由配置

| 路由 ID | 目标服务 | 路径匹配 |
|---------|---------|---------|
| social-user | lb://social-user | /api/user/** |
| social-post | lb://social-post | /api/post/** |
| social-social | lb://social-social | /api/social/** |
| social-message | lb://social-message | /api/message/** |
| social-notification | lb://social-message | /api/notification/** |
| social-message-files | lb://social-message | /uploads/** |
| social-search | lb://social-search | /api/search/** |
| social-statistics | lb://social-statistics | /api/statistics/** |
| social-ai | lb://social-ai | /api/ai/** |

### 相关文件

- `backend/social-gateway/src/main/resources/application.yml` — 路由 + Sentinel 配置
- `backend/social-gateway/pom.xml` — Gateway + Nacos Discovery 依赖
- 前端 `vite.config.js` — 开发模式代理到 Gateway 8080 端口

### 访问方式

- Nacos 控制台：`http://localhost:8848/nacos`（nacos/nacos）
- 统一网关入口：`http://localhost:8080`

---

## 第二步：RocketMQ 异步消息

### 改动内容

发布动态时，通过 RocketMQ 异步发送消息到 `social-message` 服务，由消费者写入通知表，实现业务解耦。

### 消息流程

```
PostServiceImpl.createPost()
    ↓ 发送 MQ 消息
PostMessageProducer → topic: "post-topic"
    ↓ payload: "postId:userId:contentPreview"
PostMessageConsumer (social-message)
    ↓ 解析消息，写入 notification 表
NotificationMapper.insert()
```

### 涉及服务

| 服务 | 角色 | 配置 |
|------|------|------|
| social-post (8082) | Producer | `rocketmq.name-server=127.0.0.1:9876`, `producer.group=social-producer-group` |
| social-message (8084) | Consumer | `rocketmq.name-server=127.0.0.1:9876` |

### 相关文件

- `backend/social-post/.../mq/PostMessageProducer.java` — 消息生产者
- `backend/social-post/.../service/impl/PostServiceImpl.java` — createPost 方法中调用 producer
- `backend/social-message/.../mq/PostMessageConsumer.java` — 消息消费者，写通知表

### 关键代码

**Producer** — 发送消息：
```java
public void sendPostCreatedMessage(Long postId, Long userId, String contentPreview) {
    String payload = postId + ":" + userId + ":" + (contentPreview != null ? contentPreview : "");
    rocketMQTemplate.send("post-topic", MessageBuilder.withPayload(payload).build());
}
```

**Consumer** — 消费消息：
```java
@RocketMQMessageListener(topic = "post-topic", consumerGroup = "social-message-group")
public class PostMessageConsumer implements RocketMQListener<String> {
    @Override
    public void onMessage(String message) {
        // 解析 postId:userId:contentPreview，写入 notification 表
    }
}
```

---

## 第三步：Sentinel 限流

### 改动内容

对 3 个核心接口添加 Sentinel 限流保护，通过 `@SentinelResource` 注解指定资源名和降级处理方法。

### 限流接口

| 接口 | 资源名 | 所在服务 | 注解 |
|------|--------|---------|------|
| GET /api/post/feed | `getPostFeed` | social-post | `@SentinelResource(value="getPostFeed", blockHandler="getBlockHandler")` |
| GET /api/search?keyword= | `search` | social-search | `@SentinelResource(value="search", blockHandler="searchBlockHandler")` |
| POST /api/ai/chat | `aiChat` | social-ai | `@SentinelResource(value="aiChat", blockHandler="aiChatBlockHandler")` |

### Sentinel 配置

4 个服务配置了 Sentinel Dashboard 地址：

```yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8858
      eager: true
```

| 服务 | 端口 | Sentinel 配置 |
|------|------|--------------|
| social-gateway | 8080 | ✅ |
| social-post | 8082 | ✅ |
| social-search | 8085 | ✅ |
| social-ai | 8087 | ✅ |

### 相关文件

- `backend/social-post/.../controller/PostController.java` — getPostFeed 限流
- `backend/social-search/.../controller/SearchController.java` — search 限流
- `backend/social-ai/.../controller/AiController.java` — aiChat 限流
- `backend/social-post/pom.xml`、`social-search/pom.xml`、`social-ai/pom.xml` — 添加 sentinel 依赖

### Sentinel Dashboard

- 地址：`http://localhost:8858`
- 账号：`sentinel / sentinel`
- 可在 Dashboard 上动态配置流控规则（QPS、线程数、热点参数等）

---

## 第四步：Seata 分布式事务

### 改动内容

对 2 个跨服务/跨表的业务操作添加 `@GlobalTransactional` 分布式事务注解，保证数据一致性。

### 分布式事务场景

| 场景 | 方法 | 服务 | 说明 |
|------|------|------|------|
| 发布动态 | `PostServiceImpl.createPost()` | social-post | 动态入库 + 统计更新 + MQ 通知 |
| 接受好友申请 | `FriendServiceImpl.acceptFriendRequest()` | social-social | 更新原记录状态 + 插入反向关系 |

### Seata 配置

**服务端配置**（social-post、social-social）：
```yaml
seata:
  enabled: true
  application-id: ${spring.application.name}
  tx-service-group: social-platform-tx-group
  service:
    vgroup-mapping:
      social-platform-tx-group: default
  registry:
    type: nacos
    nacos:
      application: seata-server
      server-addr: localhost:8848
  config:
    type: nacos
    nacos:
      server-addr: localhost:8848
```

**Seata Server 配置**：
- 端口：8091（TC 事务协调器）
- 控制台：7091（`http://localhost:7091`，账号 seata/seata）
- 注册中心：Nacos（SEATA_GROUP）
- 存储模式：file

### 相关文件

- `backend/social-post/.../service/impl/PostServiceImpl.java` — `@GlobalTransactional(name="createPost")`
- `backend/social-social/.../service/impl/FriendServiceImpl.java` — `@GlobalTransactional(name="acceptFriendRequest")`
- `backend/social-post/src/main/resources/application.yml` — seata 配置
- `backend/social-social/src/main/resources/application.yml` — seata 配置
- `backend/social-post/pom.xml`、`social-social/pom.xml` — seata 依赖

### Seata Server 启动

Seata Server 1.7.0 位于 `D:\development_tool\seata-server-1.7.0`（通过 subst 盘符 S: 映射）。

启动命令：
```bash
java -server -Xmx512m -Xms512m \
  -Dloader.path=S:\lib \
  -Dspring.config.location=S:\conf\application.yml \
  -Dlogging.config=S:\conf\logback-spring.xml \
  -jar S:\target\seata-server.jar
```

---

## 第五步：Docker Compose

### 改动内容

新增 `docker-compose.yml`，将所有中间件容器化编排，一条命令启动全部基础设施。

### 服务列表

| 服务 | 镜像 | 端口 |
|------|------|------|
| MySQL | mysql:8.0 | 3306 |
| Redis | redis:7-alpine | 6379 |
| Nacos | nacos/nacos-server:v2.3.0 | 8848, 9848, 9849 |
| RocketMQ NameServer | apache/rocketmq:5.1.4 | 9876 |
| RocketMQ Broker | apache/rocketmq:5.1.4 | 10911 |
| Sentinel Dashboard | bladex/sentinel-dashboard:1.8.8 | 8858 |
| Seata Server | seataio/seata-server:2.1.0 | 8091, 7091 |

### 使用方式

```bash
# 启动所有中间件
docker-compose up -d

# 查看状态
docker-compose ps

# 停止
docker-compose down
```

### 相关文件

- `docker-compose.yml` — 容器编排配置
- `start-all.bat` — 本地开发启动脚本（检查中间件 + 启动 8 个后端服务）

---

## 各服务组件分布总览

| 服务 | 端口 | Nacos | Sentinel | RocketMQ | Seata |
|------|------|-------|----------|----------|-------|
| social-gateway | 8080 | ✅ | ✅ | — | — |
| social-user | 8081 | ✅ | — | — | — |
| social-post | 8082 | ✅ | ✅ | ✅ Producer | ✅ |
| social-social | 8083 | ✅ | — | — | ✅ |
| social-message | 8084 | ✅ | — | ✅ Consumer | — |
| social-search | 8085 | ✅ | ✅ | — | — |
| social-statistics | 8086 | ✅ | — | — | — |
| social-ai | 8087 | ✅ | ✅ | — | — |

---

## 附录：关键问题修复

### MyBatis-Plus ID 策略问题

**问题**：点赞、收藏、删除对新创建的动态不生效。

**根因**：7 个服务的 `application.yml` 中全局配置了 `mybatis-plus.global-config.db-config.id-type: ASSIGN_ID`（雪花算法），覆盖了实体类上的 `@TableId(type = IdType.AUTO)` 注解。新记录使用雪花 ID 插入，但查询/删除时用的是数据库自增 ID，导致匹配失败。

**修复**：将所有 7 个服务的 `id-type` 从 `ASSIGN_ID` 改为 `AUTO`。
