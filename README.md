# SpringBoot + ShardingSphere 读写分离演示项目

## 项目简介

这是一个基于 SpringBoot + ShardingSphere + MySQL 的读写分离演示项目，展示了以下核心功能：

1. **ShardingSphere 读写分离**：主业务数据库自动进行读写分离
2. **@DS注解支持**：使用 Baomidou Dynamic DataSource 支持多数据源切换
3. **混合架构**：ShardingSphere 和 @DS 注解并存，各司其职
4. **强制主库查询**：支持特殊场景下强制读取主库

## 技术栈

- SpringBoot 3.2.0
- ShardingSphere JDBC 5.4.1
- MySQL 8.0 (主从复制)
- MyBatis 3.0.3
- Baomidou Dynamic DataSource 4.3.0
- Docker & Docker Compose

## 系统架构

```
┌─────────────────────────────────────────┐
│              应用层                      │
│  ┌─────────────┐  ┌─────────────────────┐│
│  │ UserService │  │   ConfigService     ││
│  │(读写分离)    │  │   (@DS注解)        ││
│  └─────────────┘  └─────────────────────┘│
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│            数据源路由层                   │
│  ┌─────────────┐  ┌─────────────────────┐│
│  │ShardingSphere│  │ Baomidou Dynamic   ││
│  │   路由层     │  │   DataSource       ││
│  └─────────────┘  └─────────────────────┘│
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│              数据库层                    │
│ ┌──────────┐┌──────────┐ ┌─────────────┐│
│ │主库:3306 ││从库:3307 │ │ 配置库:3308  ││
│ │  master  ││  slave   │ │   config    ││
│ │   ↓ 同步  ││    ↑     │ │  (单库)     ││
│ └──────────┘└──────────┘ └─────────────┘│
└─────────────────────────────────────────┘
```

## 快速开始

### 1. 启动 MySQL 主从环境

```bash
cd docker/mysql
docker-compose up -d
```

等待 30 秒让主从同步完成，然后验证：

```bash
# 查看容器状态
docker-compose ps

# 验证主从同步状态
docker exec mysql-slave mysql -uroot -proot123 -e "SHOW SLAVE STATUS\G" | grep "Slave_IO_Running\|Slave_SQL_Running"
```

### 2. 启动应用

```bash
mvn spring-boot:run
```

或使用IDE启动 `ReadWriteDemoApplication`

### 3. 验证功能

应用启动后，访问：http://localhost:8080/api/status

## API 测试指南

### ShardingSphere 读写分离测试

#### 1. 查询操作（自动路由到从库）

```bash
# 查询所有用户
curl http://localhost:8080/api/users

# 根据ID查询用户
curl http://localhost:8080/api/users/1

# 统计用户总数
curl http://localhost:8080/api/users/count

# 搜索用户
curl "http://localhost:8080/api/users/search?username=john_doe"
```

#### 2. 写入操作（自动路由到主库）

```bash
# 创建用户
curl -X POST "http://localhost:8080/api/users" \
  -d "username=test_user&email=test@example.com&age=30"

# 更新用户
curl -X PUT "http://localhost:8080/api/users/1" \
  -d "username=updated_user&email=updated@example.com&age=31"

# 删除用户
curl -X DELETE http://localhost:8080/api/users/1
```

#### 3. 强制主库查询

```bash
# 强制从主库查询（用于认证等场景）
curl http://localhost:8080/api/users/1/auth
```

### @DS 注解多数据源测试

#### 1. 配置库操作

```bash
# 查询所有配置
curl http://localhost:8080/mixed/config

# 根据键查询配置
curl http://localhost:8080/mixed/config/app.name

# 根据ID查询配置
curl http://localhost:8080/mixed/config/id/1

# 创建配置
curl -X POST "http://localhost:8080/mixed/config" \
  -d "key=test.key&value=test.value&description=测试配置"

# 更新配置
curl -X PUT "http://localhost:8080/mixed/config/test.key" \
  -d "value=new.value&description=更新后的配置"

# 删除配置
curl -X DELETE http://localhost:8080/mixed/config/1
```

#### 2. 混合场景测试

```bash
# 主业务库查询（走从库）
curl http://localhost:8080/mixed/users

# 主业务库写入（走主库）
curl -X POST "http://localhost:8080/mixed/users" \
  -d "username=mixed_test&email=mixed@test.com&age=25"

# 主从同步验证
curl http://localhost:8080/mixed/sync-test
```

## 核心特性详解

### 1. 自动读写分离

- **读操作**：SELECT 语句自动路由到从库（slave:3307）
- **写操作**：INSERT/UPDATE/DELETE 自动路由到主库（master:3306）
- **事务内查询**：事务内的所有操作都路由到主库，保证数据一致性

### 2. 强制主库查询

使用 `HintManager` 可以强制查询操作路由到主库：

```java
try (HintManager hintManager = HintManager.getInstance()) {
    hintManager.setWriteRouteOnly();
    User user = userService.findUserById(id);
}
```

### 3. @DS 注解支持

```java
@DS("config")
public SystemConfig getConfigByKey(String configKey) {
    return configMapper.selectByConfigKey(configKey);
}
```

### 4. 事务管理

```java
@Transactional  // 事务内的读操作也会路由到主库
public User createUser(String username, String email, Integer age) {
    // 此方法内的所有数据库操作都在主库执行
}
```

## 监控和日志

应用启动时会显示详细的路由信息：

```
📖 [SLAVE-DB] 查询所有用户 - ShardingSphere自动路由到从库
✍️ [MASTER-DB] 创建用户: test_user - ShardingSphere自动路由到主库
🔧 [CONFIG-DB] 查询配置: app.name - 使用@DS("config")注解访问独立配置库
```

## 配置说明

### ShardingSphere 配置

```yaml
spring:
  shardingsphere:
    datasource:
      names: master,slave
      master:
        jdbc-url: jdbc:mysql://localhost:3306/readwrite_demo
      slave:
        jdbc-url: jdbc:mysql://localhost:3307/readwrite_demo
    rules:
      readwrite-splitting:
        data-sources:
          readwrite-ds:
            static-strategy:
              write-data-source-name: master
              read-data-source-names: slave
```

### Dynamic DataSource 配置

```yaml
spring:
  datasource:
    dynamic:
      primary: master
      datasource:
        config:
          url: jdbc:mysql://localhost:3308/config_db
```

## 部署到生产环境

### 1. 数据库准备

1. 准备MySQL主从环境
2. 执行 `docker/mysql/scripts/` 中的初始化脚本
3. 验证主从同步正常

### 2. 应用配置

1. 修改 `application-shardingsphere.yml` 中的数据库连接信息
2. 调整连接池配置
3. 配置生产环境的日志级别

### 3. 启动应用

```bash
java -jar readwrite-demo-1.0.0.jar --spring.profiles.active=shardingsphere
```

## 常见问题

### Q: 主从同步延迟怎么处理？
A: 
1. 使用 HintManager 强制重要查询走主库
2. 监控主从同步延迟
3. 业务上容忍一定的延迟

### Q: 事务中的查询为什么走主库？
A: ShardingSphere 保证事务内数据一致性，自动将事务内所有操作路由到主库。

### Q: 如何验证读写分离是否生效？
A: 
1. 查看应用日志中的路由信息
2. 启用 ShardingSphere 的 SQL 日志
3. 监控主从数据库的连接数

### Q: @DS 注解和 ShardingSphere 冲突吗？
A: 不冲突。ShardingSphere 管理主从分离的数据源，@DS 管理其他独立数据源。

## 项目结构

```
src/main/java/com/demo/readwrite/
├── entity/                 # 实体类
│   ├── User.java
│   └── SystemConfig.java
├── mapper/                 # MyBatis Mapper接口
│   ├── UserMapper.java
│   └── SystemConfigMapper.java
├── service/                # 业务服务层
│   ├── UserService.java
│   └── ConfigService.java
├── controller/             # 控制器层
│   ├── ReadWriteController.java
│   └── MixedDataSourceController.java
└── ReadWriteDemoApplication.java

docker/mysql/              # MySQL主从Docker配置
├── docker-compose.yml
├── master/my.cnf
├── slave/my.cnf
└── scripts/
    ├── master-init.sql
    ├── slave-init.sql
    ├── demo-data.sql
    └── config-init.sql
```

## 扩展建议

1. **监控**: 集成 Prometheus + Grafana 监控数据库性能
2. **缓存**: 添加 Redis 缓存减少从库查询压力  
3. **负载均衡**: 配置多个从库实现读负载均衡
4. **数据校验**: 定期校验主从数据一致性
5. **熔断降级**: 添加数据库熔断机制

## 联系方式

如有问题或建议，请提交 Issue 或 Pull Request。