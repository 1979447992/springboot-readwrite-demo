# Spring Boot + MyBatis Plus 读写分离Demo

## 📋 项目概述

本项目是一个完整的Spring Boot读写分离示例应用，使用MyBatis Plus和Oracle数据库，实现了动态数据源路由功能。项目包含用户管理、支付处理和账单管理三个核心业务模块，特别针对支付等关键操作实现了强制主库查询功能。

### 🏗️ 核心架构

```
┌─────────────────────────────────────────────────────────┐
│                    Client Layer                        │
├─────────────────────────────────────────────────────────┤
│                 REST Controller                         │
├─────────────────────────────────────────────────────────┤
│                  Service Layer                          │
├─────────────────────────────────────────────────────────┤
│                 AOP Aspect                              │
│           (数据源路由决策)                                 │
├─────────────────────────────────────────────────────────┤
│              Dynamic DataSource Router                  │
│  ┌─────────────────┐    ┌─────────────────┐              │
│  │    Master DB    │◄──►│    Slave DB     │              │
│  │   (写 + 强制读)   │    │   (默认读操作)    │              │  
│  └─────────────────┘    └─────────────────┘              │
└─────────────────────────────────────────────────────────┘
```

### ⭐ 核心特性

1. **动态数据源路由** - 自动根据操作类型切换主从库
2. **@MasterDB注解** - 支付等关键操作强制查询主库
3. **AOP透明切换** - 业务代码无感知的数据源切换
4. **完整业务场景** - 用户、支付、账单等真实业务模块
5. **全面测试覆盖** - 单元测试和集成测试
6. **容器化部署** - Docker和Docker Compose支持
7. **生产就绪** - Ubuntu服务器部署脚本

## 🚀 快速开始

### 环境要求

- Java 17+
- Maven 3.6+
- Oracle 11g+ 或 Oracle XE
- Ubuntu 20.04+ (生产环境)
- Docker & Docker Compose (可选)

### 本地开发环境

1. **克隆项目**
```bash
git clone <repository-url>
cd springboot-readwrite-demo
```

2. **配置数据库**
```bash
# 启动Oracle数据库
# 执行数据库初始化脚本
sqlplus sys/password@localhost:1521/XE as sysdba @src/main/resources/sql/init-users.sql
sqlplus MASTER_USER/master_password@localhost:1521/XEPDB1 @src/main/resources/sql/schema.sql
sqlplus MASTER_USER/master_password@localhost:1521/XEPDB1 @src/main/resources/sql/data.sql
```

3. **修改配置文件**
```yaml
# src/main/resources/application.yml
spring:
  datasource:
    master:
      jdbc-url: jdbc:oracle:thin:@//localhost:1521/XEPDB1
      username: MASTER_USER
      password: master_password
    slave:
      jdbc-url: jdbc:oracle:thin:@//localhost:1521/XEPDB1
      username: SLAVE_USER
      password: slave_password
```

4. **编译运行**
```bash
mvn clean package -DskipTests
java -jar target/springboot-readwrite-demo-1.0.0.jar
```

5. **验证运行**
```bash
curl http://localhost:8080/api/health
```

## 🔧 关键组件详解

### 1. 动态数据源路由

**核心类**: `DynamicDataSource.java`
- 继承`AbstractRoutingDataSource`
- 根据`ThreadLocal`中的数据源类型动态选择数据源

**上下文管理**: `DataSourceContextHolder.java`
- 使用`ThreadLocal`存储当前线程的数据源类型
- 提供线程安全的数据源切换方法

### 2. @MasterDB注解机制

**注解定义**: `@MasterDB`
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MasterDB {
    String value() default "强制使用主库";
}
```

**AOP切面**: `DataSourceAspect.java`
- 拦截带有`@MasterDB`注解的方法
- 自动识别写操作（save/insert/update/delete等方法前缀）
- 确保事务完成后清理上下文

### 3. 业务场景设计

#### 支付服务 (PaymentService)
```java
@Service
@MasterDB("支付服务所有操作强制使用主库")
public class PaymentService {
    
    @MasterDB("支付状态查询必须强制主库")
    public Payment getPaymentById(Long id) {
        return getById(id);
    }
    
    @MasterDB("处理支付必须强制主库")
    public boolean processPayment(String orderNo) {
        // 支付处理逻辑
    }
}
```

#### 用户服务 (UserService)
```java
@Service
public class UserService {
    
    // 普通查询使用从库
    public List<User> getAllUsers() {
        return list();
    }
    
    // 用户认证强制主库
    @MasterDB("用户认证需要强制主库查询")
    public User authenticate(String username, String password) {
        // 认证逻辑
    }
}
```

## 📊 API接口文档

### 健康检查
```http
GET /api/health
```
**响应示例**:
```json
{
  "status": "UP",
  "application": "ReadWrite Demo",
  "version": "1.0.0",
  "timestamp": 1703123456789,
  "currentDataSource": "slave"
}
```

### 用户管理

#### 创建用户
```http
POST /api/users
Content-Type: application/x-www-form-urlencoded

username=testuser&email=test@demo.com&phone=13800000001&password=password123
```

#### 用户认证 (强制主库)
```http
POST /api/users/authenticate
Content-Type: application/x-www-form-urlencoded

username=testuser&password=password123
```

#### 查询用户
```http
GET /api/users/1001
```

### 支付管理 (所有操作强制主库)

#### 创建支付
```http
POST /api/payments
Content-Type: application/x-www-form-urlencoded

userId=1001&amount=99.99&paymentMethod=微信支付&description=VIP会员
```

#### 处理支付
```http
POST /api/payments/process
Content-Type: application/x-www-form-urlencoded

orderNo=ORDER_1700000001_ABC123
```

#### 查询支付状态
```http
GET /api/payments/order/ORDER_1700000001_ABC123
```

### 账单管理

#### 创建账单
```http
POST /api/bills
Content-Type: application/x-www-form-urlencoded

userId=1001&title=工资收入&amount=8000.00&type=1&remark=11月工资
```

#### 账单统计 (强制主库)
```http
GET /api/bills/user/1001/total
```

## 🧪 测试验证

### 1. 单元测试
```bash
mvn test
```

### 2. 读写分离验证

#### 验证读操作使用从库
```bash
# 查询用户列表 (应该使用从库)
curl http://localhost:8080/api/users

# 查看日志确认数据源
tail -f logs/readwrite-demo.log | grep "使用从库"
```

#### 验证写操作使用主库
```bash
# 创建用户 (应该使用主库)
curl -X POST http://localhost:8080/api/users \
  -d "username=newuser&email=new@demo.com&phone=13900000001&password=pass123"

# 查看日志确认数据源
tail -f logs/readwrite-demo.log | grep "使用主库"
```

#### 验证@MasterDB注解
```bash
# 用户认证 (强制主库)
curl -X POST http://localhost:8080/api/users/authenticate \
  -d "username=admin&password=admin123"

# 支付查询 (强制主库)
curl http://localhost:8080/api/payments/2001

# 查看日志确认强制主库
tail -f logs/readwrite-demo.log | grep "强制主库"
```

### 3. 压力测试

使用Apache Bench进行压力测试：
```bash
# 测试读操作性能
ab -n 1000 -c 10 http://localhost:8080/api/users

# 测试写操作性能
ab -n 100 -c 5 -p post_data.txt -T application/x-www-form-urlencoded \
   http://localhost:8080/api/bills
```

## 🚀 生产部署

### Ubuntu服务器部署

1. **准备服务器**
```bash
# 上传部署脚本到服务器
scp deploy.sh root@your-server:/tmp/
scp deploy-db.sh root@your-server:/tmp/

# 执行部署
ssh root@your-server
chmod +x /tmp/deploy.sh
./tmp/deploy.sh
```

2. **配置数据库**
```bash
# 上传SQL脚本
scp -r src/main/resources/sql/ root@your-server:/tmp/

# 执行数据库配置
chmod +x /tmp/deploy-db.sh
./tmp/deploy-db.sh
```

3. **部署应用**
```bash
# 编译应用
mvn clean package -DskipTests

# 上传JAR文件
scp target/springboot-readwrite-demo-1.0.0.jar root@your-server:/opt/readwrite-demo/app.jar

# 启动应用
systemctl start readwrite-demo.service
systemctl enable readwrite-demo.service
```

4. **验证部署**
```bash
# 检查服务状态
systemctl status readwrite-demo.service

# 查看日志
journalctl -u readwrite-demo.service -f

# 测试接口
curl http://your-server-ip/api/health
```

### Docker部署

1. **使用Docker Compose**
```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f readwrite-app
```

2. **验证容器部署**
```bash
# 检查Oracle数据库
docker exec -it oracle-xe sqlplus sys/OraclePassword123@localhost:1521/XE as sysdba

# 检查应用
curl http://localhost:8080/api/health
```

## 🔍 监控和运维

### 1. 日志监控
```bash
# 应用日志
tail -f /var/log/readwrite-demo/readwrite-demo.log

# 系统日志
journalctl -u readwrite-demo.service -f

# 数据源切换日志
grep "数据源" /var/log/readwrite-demo/readwrite-demo.log
```

### 2. 性能监控
```bash
# JVM监控
jstat -gc [PID] 1s

# 数据库连接池监控
curl http://localhost:8080/api/health | jq '.datasource'
```

### 3. 故障排查

#### 数据库连接问题
```bash
# 检查Oracle服务
lsnrctl status

# 测试数据库连接
sqlplus MASTER_USER/master_password@localhost:1521/XEPDB1
sqlplus SLAVE_USER/slave_password@localhost:1521/XEPDB1
```

#### 应用启动问题
```bash
# 检查端口占用
netstat -tulpn | grep 8080

# 检查Java进程
jps -v

# 检查配置文件
cat /opt/readwrite-demo/config/application.yml
```

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 许可证

本项目使用 MIT 许可证。详情请参阅 [LICENSE](LICENSE) 文件。

## 📞 技术支持

- 项目文档: [项目Wiki](https://github.com/your-repo/wiki)
- 问题反馈: [Issues](https://github.com/your-repo/issues)
- 技术讨论: [Discussions](https://github.com/your-repo/discussions)