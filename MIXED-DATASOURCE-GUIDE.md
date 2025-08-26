# 混合数据源部署和测试指南

## 架构概述

本项目实现了混合数据源架构：
- **主业务库**: 读写分离 (master:5432 + slave:5433)
- **配置库**: @DS("config") 单库 (localhost:5434) 
- **日志库**: @DS("log") 单库 (localhost:5435)

## 1. 环境准备

### 1.1 启动所有PostgreSQL数据库

```bash
cd docker/postgres
docker-compose up -d
```

这将启动4个PostgreSQL容器：
- pg-master (5432) - 主业务库主库
- pg-slave (5433) - 主业务库从库 
- pg-config (5434) - 配置数据库
- pg-log (5435) - 日志数据库

### 1.2 验证数据库启动状态

```bash
docker ps | grep postgres
```

预期输出：4个运行中的PostgreSQL容器

### 1.3 验证数据库连接

```bash
# 测试主库连接
docker exec -it pg-master psql -U postgres -d readwrite_demo -c "SELECT 1;"

# 测试配置库连接
docker exec -it pg-config psql -U postgres -d config_db -c "SELECT * FROM config_settings LIMIT 3;"

# 测试日志库连接
docker exec -it pg-log psql -U postgres -d log_db -c "SELECT * FROM application_logs LIMIT 3;"
```

## 2. 启动Spring Boot应用

```bash
# 使用混合数据源配置启动
mvn spring-boot:run -Dspring-boot.run.profiles=multi-ds
```

或者：

```bash
# 编译并运行
mvn clean package
java -jar target/readwrite-demo-1.0.0.jar --spring.profiles.active=multi-ds
```

## 3. 功能测试

### 3.1 系统状态检查

```bash
curl http://localhost:8080/mixed/status
```

### 3.2 主业务库读写分离测试

```bash
# 查询用户 (应该路由到SLAVE库)
curl http://localhost:8080/mixed/users

# 创建用户 (应该路由到MASTER库)
curl -X POST http://localhost:8080/mixed/users \
  -d "username=张三" \
  -d "email=zhangsan@demo.com"

# 验证主从同步
curl http://localhost:8080/mixed/sync-test
```

### 3.3 配置库 @DS("config") 测试

```bash
# 查询所有配置
curl http://localhost:8080/mixed/config

# 查询单个配置  
curl http://localhost:8080/mixed/config/max_upload_size

# 按组查询配置
curl http://localhost:8080/mixed/config/group/system

# 创建配置
curl -X POST http://localhost:8080/mixed/config \
  -d "key=new_setting" \
  -d "value=test_value" \
  -d "description=测试配置" \
  -d "group=test"

# 更新配置
curl -X PUT http://localhost:8080/mixed/config/enable_cache \
  -d "value=false"

# 删除配置 (软删除)
curl -X DELETE http://localhost:8080/mixed/config/new_setting

# 查询配置分类
curl http://localhost:8080/mixed/config/categories
```

### 3.4 日志库 @DS("log") 测试

```bash
# 写入应用日志
curl -X POST http://localhost:8080/mixed/log/app \
  -d "level=INFO" \
  -d "logger=TestController" \
  -d "message=测试应用日志" \
  -d "thread=http-thread" \
  -d "userId=1001" \
  -d "sessionId=test_session"

# 写入审计日志
curl -X POST http://localhost:8080/mixed/log/audit \
  -d "userId=1001" \
  -d "username=admin" \
  -d "action=CREATE" \
  -d "resource=user" \
  -d "resourceId=123" \
  -d "oldValue={}" \
  -d "newValue={\"name\":\"张三\"}" \
  -d "ipAddress=192.168.1.100"

# 记录系统指标
curl -X POST http://localhost:8080/mixed/log/metric \
  -d "name=cpu_usage" \
  -d "value=85.5" \
  -d "unit=percent" \
  -d "tags=server:web-01"

# 查询应用日志
curl "http://localhost:8080/mixed/log/app?level=INFO&limit=10"

# 查询审计日志
curl "http://localhost:8080/mixed/log/audit?userId=1001&limit=10"

# 查询系统指标
curl "http://localhost:8080/mixed/log/metric/cpu_usage?hours=24"
```

## 4. 验证数据源路由

### 4.1 观察控制台日志

启动应用后，每次API调用都会在控制台打印数据源路由信息：

- `👥 [MASTER-DB]` - 主业务库写操作  
- `👥 [SLAVE-DB]` - 主业务库读操作
- `🔧 [CONFIG-DB]` - 配置库操作
- `📝 [LOG-DB]` - 日志库操作

### 4.2 数据库数据验证

```bash
# 检查主业务库数据同步
docker exec -it pg-master psql -U postgres -d readwrite_demo -c "SELECT COUNT(*) FROM users;"
docker exec -it pg-slave psql -U postgres -d readwrite_demo -c "SELECT COUNT(*) FROM users;"

# 检查配置库数据
docker exec -it pg-config psql -U postgres -d config_db -c "SELECT config_key, config_value FROM config_settings LIMIT 5;"

# 检查日志库数据  
docker exec -it pg-log psql -U postgres -d log_db -c "SELECT log_level, message FROM application_logs ORDER BY timestamp DESC LIMIT 5;"
```

## 5. 自动化测试脚本

使用提供的测试脚本：

```bash
chmod +x test-mixed-datasource.sh
./test-mixed-datasource.sh
```

## 6. 预期行为验证

### ✅ 正确的数据源路由

1. **主业务库查询** → 自动路由到SLAVE (5433)
2. **主业务库写入** → 自动路由到MASTER (5432)
3. **配置库操作** → 强制使用@DS("config") (5434)
4. **日志库操作** → 强制使用@DS("log") (5435)

### ✅ 混合架构优势

1. **核心业务库**: 享受读写分离性能优势
2. **辅助数据库**: 使用@DS注解灵活指定，无需复杂配置
3. **优先级处理**: @DS注解优先级高于自动读写分离
4. **真实数据**: 所有操作都连接真实数据库，非模拟数据

## 7. 故障排除

### 7.1 数据库连接问题

```bash
# 检查容器状态
docker ps -a | grep postgres

# 查看容器日志
docker logs pg-master
docker logs pg-slave  
docker logs pg-config
docker logs pg-log
```

### 7.2 主从复制问题

```bash
# 检查复制状态
docker exec -it pg-master psql -U postgres -c "SELECT * FROM pg_stat_replication;"
docker exec -it pg-slave psql -U postgres -c "SELECT * FROM pg_stat_wal_receiver;"
```

### 7.3 应用启动问题

检查application-multi-ds.yml中的数据库连接配置是否与Docker容器端口匹配。

## 8. 生产环境部署

在生产环境中：
1. 将Docker容器替换为实际的PostgreSQL服务器
2. 修改application-multi-ds.yml中的数据库连接信息
3. 确保主从复制正确配置并测试
4. 添加连接池监控和数据库健康检查