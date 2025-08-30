# SpringBoot Read-Write Separation Demo

基于 `dynamic-datasource-spring-boot-starter 3.6.1` 的 Spring Boot 读写分离解决方案。

## 🎯 核心功能

1. **自动读写分离** - SQL自动路由，写操作→主库，读操作→从库
2. **@DS业务数据库不受影响** - 与现有@DS注解完全兼容
3. **@MasterOnly强制主库读取** - 特殊场景强制从主库读取最新数据

## 🏗️ 技术架构

- **Spring Boot**: 3.1.5
- **Dynamic DataSource**: 3.6.1  
- **MyBatis Plus**: 3.5.4
- **MySQL**: 8.0
- **Docker**: 容器化部署
- **Java**: 17

## 📋 目录结构

```
src/main/java/com/demo/readwrite/
├── annotation/
│   └── MasterOnly.java              # 强制主库读取注解
├── aspect/
│   ├── MasterOnlyAspect.java        # @MasterOnly切面处理
│   └── TransactionAspect.java       # 事务切面处理
├── config/
│   └── DataSourceConfig.java       # 数据源配置
├── controller/
│   ├── HealthController.java       # 健康检查
│   └── UserController.java         # 用户API
├── entity/
│   └── User.java                   # 用户实体
├── interceptor/
│   └── ReadWriteSplitInterceptor.java # SQL拦截器
├── mapper/
│   └── UserMapper.java             # MyBatis映射
├── service/
│   └── UserService.java            # 用户服务
└── strategy/
    └── SimpleReadWriteStrategy.java # 读写分离策略
```

## ⚙️ 配置说明

### application-dynamic.yml
```yaml
spring:
  datasource:
    dynamic:
      primary: master  # 默认主数据源
      strategy: com.demo.readwrite.strategy.SimpleReadWriteStrategy
      datasource:
        master:  # 主库配置
          url: jdbc:mysql://mysql-master:3306/readwrite_demo
          username: root
          password: root123
        slave:   # 从库配置  
          url: jdbc:mysql://mysql-slave:3306/readwrite_demo
          username: root
          password: root123
```

## 🚀 快速启动

### 1. 启动数据库
```bash
docker-compose up -d mysql-master mysql-slave
```

### 2. 构建应用
```bash
docker build -t readwrite-demo:latest .
```

### 3. 启动应用
```bash
docker run -d --name readwrite-demo \
  --network springboot-readwrite-demo_default \
  -p 8080:8080 readwrite-demo:latest
```

## 🧪 功能测试

### 写操作（路由到master）
```bash
curl -X POST "http://localhost:8080/api/users" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=testuser&email=test@example.com&age=25"
```

### 读操作（路由到slave）
```bash  
curl "http://localhost:8080/api/users"
```

### @MasterOnly强制主库读取
```bash
curl "http://localhost:8080/api/users/1/master"
```

### 查看路由日志
```bash
docker logs readwrite-demo | grep -E "(SQL拦截|检测到强制路由)" | tail -10
```

## 📊 预期日志输出

**写操作日志：**
```
SQL拦截 - 类型: INSERT, 路由: WRITE, SQL: INSERT INTO demo_user ...
```

**读操作日志：**
```  
SQL拦截 - 类型: SELECT, 路由: READ, SQL: SELECT * FROM demo_user
```

**@MasterOnly日志：**
```
检测到强制路由设置，保持: WRITE
SQL拦截 - 类型: SELECT, 路由: WRITE, SQL: SELECT * FROM demo_user WHERE id=?
```

## 🔧 核心实现

### 1. SQL拦截器
- `ReadWriteSplitInterceptor` - 拦截所有SQL操作
- 自动识别INSERT/UPDATE/DELETE→WRITE，SELECT→READ
- 支持复杂场景：FOR UPDATE、事务等强制主库

### 2. @MasterOnly注解
- `@MasterOnly` - 方法级别强制主库读取
- `MasterOnlyAspect` - AOP切面处理
- 优先级高于自动路由策略

### 3. 数据源策略  
- `SimpleReadWriteStrategy` - 实现路由决策
- ThreadLocal管理SQL类型和事务状态
- 支持K8s环境下的负载均衡

## 🌟 设计亮点

1. **最小化侵入** - 只需配置文件修改，业务代码无需调整
2. **智能路由** - 自动识别SQL类型并路由
3. **事务感知** - 事务中所有操作强制主库
4. **强制控制** - @MasterOnly提供精确控制
5. **K8s友好** - 适配云原生环境

## 📝 注意事项

1. **主从同步** - 生产环境需配置MySQL主从复制
2. **事务一致性** - 事务中的所有操作都会路由到主库
3. **连接池配置** - 根据实际负载调整连接池参数
4. **监控告警** - 建议配置数据库连接和延迟监控

## 🔍 故障排查

### 应用无法启动
```bash
docker logs readwrite-demo | grep ERROR
```

### 路由不生效
```bash  
docker logs readwrite-demo | grep "SQL拦截" | tail -20
```

### 数据库连接问题
```bash
docker logs readwrite-demo | grep -E "(datasource|connection)" | tail -10
```

## 📚 参考资料

- [Dynamic DataSource 官方文档](https://baomidou.com/pages/a61e1b/)
- [MyBatis Plus 官方文档](https://baomidou.com/)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
