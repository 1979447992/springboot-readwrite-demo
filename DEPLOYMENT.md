# 部署和验证指南

## 🎯 部署验证清单

### 阶段一：环境准备验证

#### ✅ 系统环境检查
- [ ] Ubuntu 20.04+ 操作系统
- [ ] JDK 17 安装并配置环境变量
- [ ] Maven 3.6+ 安装
- [ ] Oracle数据库服务运行正常
- [ ] 防火墙配置正确

**验证命令:**
```bash
# 检查系统版本
lsb_release -a

# 检查Java版本
java -version
javac -version

# 检查Maven版本
mvn -version

# 检查Oracle服务
sudo systemctl status oracle-xe
lsnrctl status
```

### 阶段二：数据库配置验证

#### ✅ Oracle数据库验证
- [ ] Oracle XE服务启动成功
- [ ] 系统用户连接正常
- [ ] MASTER_USER创建成功
- [ ] SLAVE_USER创建成功
- [ ] 表结构创建完成
- [ ] 测试数据插入成功
- [ ] 用户权限配置正确

**验证脚本:**
```bash
#!/bin/bash
echo "=== Oracle数据库验证 ==="

# 1. 检查Oracle服务状态
echo "1. 检查Oracle服务状态"
systemctl is-active oracle-xe

# 2. 检查监听器状态
echo "2. 检查监听器状态"
lsnrctl status | grep "Service" | head -5

# 3. 测试系统用户连接
echo "3. 测试系统用户连接"
sqlplus -S sys/OraclePassword123@localhost:1521/XE as sysdba <<< "SELECT 'SYS连接成功' FROM DUAL;"

# 4. 测试主库用户连接
echo "4. 测试主库用户连接"
sqlplus -S MASTER_USER/master_password@localhost:1521/XEPDB1 <<< "SELECT '主库用户连接成功', COUNT(*) AS 用户数 FROM users;"

# 5. 测试从库用户连接
echo "5. 测试从库用户连接"
sqlplus -S SLAVE_USER/slave_password@localhost:1521/XEPDB1 <<< "SELECT '从库用户连接成功', COUNT(*) AS 用户数 FROM users;"

# 6. 验证权限配置
echo "6. 验证权限配置"
sqlplus -S SLAVE_USER/slave_password@localhost:1521/XEPDB1 <<EOF
SELECT 
    '从库用户权限验证',
    (SELECT COUNT(*) FROM users) AS users_count,
    (SELECT COUNT(*) FROM payments) AS payments_count,
    (SELECT COUNT(*) FROM bills) AS bills_count
FROM DUAL;
EOF

echo "=== 数据库验证完成 ==="
```

### 阶段三：应用部署验证

#### ✅ Spring Boot应用验证
- [ ] JAR文件编译成功
- [ ] 配置文件正确
- [ ] 应用启动成功
- [ ] 健康检查通过
- [ ] 数据源连接正常
- [ ] 接口响应正常

**验证脚本:**
```bash
#!/bin/bash
echo "=== Spring Boot应用验证 ==="

# 1. 检查JAR文件
echo "1. 检查JAR文件"
if [ -f "/opt/readwrite-demo/app.jar" ]; then
    echo "✅ JAR文件存在"
    ls -lh /opt/readwrite-demo/app.jar
else
    echo "❌ JAR文件不存在"
    exit 1
fi

# 2. 检查服务状态
echo "2. 检查服务状态"
systemctl is-active readwrite-demo.service
if [ $? -eq 0 ]; then
    echo "✅ 服务运行正常"
else
    echo "❌ 服务未运行，查看日志:"
    systemctl status readwrite-demo.service
    exit 1
fi

# 3. 检查端口监听
echo "3. 检查端口监听"
netstat -tulpn | grep 8080
if [ $? -eq 0 ]; then
    echo "✅ 端口8080监听正常"
else
    echo "❌ 端口8080未监听"
    exit 1
fi

# 4. 健康检查
echo "4. 应用健康检查"
health_response=$(curl -s http://localhost:8080/api/health)
if [[ $health_response == *"UP"* ]]; then
    echo "✅ 健康检查通过"
    echo "$health_response" | jq .
else
    echo "❌ 健康检查失败"
    echo "$health_response"
    exit 1
fi

echo "=== 应用验证完成 ==="
```

### 阶段四：读写分离功能验证

#### ✅ 数据源路由验证

**1. 读操作验证(应该使用从库)**
```bash
#!/bin/bash
echo "=== 读操作验证 ==="

# 清理日志，便于观察
sudo journalctl --vacuum-time=1d

# 执行读操作
echo "执行用户列表查询..."
curl -s http://localhost:8080/api/users > /dev/null

echo "执行账单查询..."
curl -s http://localhost:8080/api/bills > /dev/null

# 检查日志中的数据源使用情况
echo "检查数据源使用情况:"
journalctl -u readwrite-demo.service --since="1 minute ago" | grep -E "(使用从库|使用主库)" | tail -5

echo "=== 读操作验证完成 ==="
```

**2. 写操作验证(应该使用主库)**
```bash
#!/bin/bash
echo "=== 写操作验证 ==="

# 执行写操作
echo "创建新用户..."
create_response=$(curl -s -X POST http://localhost:8080/api/users \
  -d "username=test_$(date +%s)&email=test_$(date +%s)@demo.com&phone=138$(date +%s | tail -c 9)&password=test123")

echo "创建账单..."
curl -s -X POST http://localhost:8080/api/bills \
  -d "userId=1001&title=测试账单_$(date +%s)&amount=100.00&type=1&remark=验证测试" > /dev/null

# 检查日志
echo "检查数据源使用情况:"
journalctl -u readwrite-demo.service --since="1 minute ago" | grep -E "(使用主库)" | tail -5

echo "=== 写操作验证完成 ==="
```

**3. @MasterDB注解验证(强制使用主库)**
```bash
#!/bin/bash
echo "=== @MasterDB注解验证 ==="

# 执行用户认证（带@MasterDB注解）
echo "执行用户认证..."
auth_response=$(curl -s -X POST http://localhost:8080/api/users/authenticate \
  -d "username=admin&password=admin123")

# 执行支付查询（带@MasterDB注解）
echo "执行支付查询..."
payment_response=$(curl -s http://localhost:8080/api/payments/2001)

# 执行支付状态统计（带@MasterDB注解）
echo "执行支付状态统计..."
status_response=$(curl -s http://localhost:8080/api/payments/status/1)

# 检查日志中的强制主库使用
echo "检查强制主库使用情况:"
journalctl -u readwrite-demo.service --since="2 minutes ago" | grep -E "(强制.*主库|@MasterDB)" | tail -10

echo "=== @MasterDB注解验证完成 ==="
```

### 阶段五：性能和压力验证

#### ✅ 基础性能测试
```bash
#!/bin/bash
echo "=== 性能测试 ==="

# 检查系统资源
echo "1. 系统资源使用情况:"
echo "CPU使用率:"
top -bn1 | grep "Cpu(s)" | sed "s/.*, *\([0-9.]*\)%* id.*/\1/" | awk '{print 100 - $1"%"}'

echo "内存使用情况:"
free -h

echo "磁盘使用情况:"
df -h | grep -E '/$|/opt'

# 2. 应用响应时间测试
echo "2. 应用响应时间测试:"
echo "健康检查响应时间:"
curl -w "@curl-format.txt" -o /dev/null -s http://localhost:8080/api/health

echo "用户列表查询响应时间:"
curl -w "@curl-format.txt" -o /dev/null -s http://localhost:8080/api/users

# 3. 数据库连接池状态
echo "3. 数据库连接池状态:"
curl -s http://localhost:8080/api/health | jq '.datasource // "未配置数据源监控"'

echo "=== 性能测试完成 ==="
```

#### ✅ 并发测试
```bash
#!/bin/bash
echo "=== 并发测试 ==="

# 安装Apache Bench (如果未安装)
if ! command -v ab &> /dev/null; then
    echo "安装Apache Bench..."
    sudo apt-get update && sudo apt-get install -y apache2-utils
fi

# 创建curl格式化文件
cat > curl-format.txt << 'EOF'
     time_namelookup:  %{time_namelookup}\n
        time_connect:  %{time_connect}\n
     time_appconnect:  %{time_appconnect}\n
    time_pretransfer:  %{time_pretransfer}\n
       time_redirect:  %{time_redirect}\n
  time_starttransfer:  %{time_starttransfer}\n
                     ----------\n
          time_total:  %{time_total}\n
EOF

echo "1. 读操作并发测试 (1000次请求，10个并发):"
ab -n 1000 -c 10 http://localhost:8080/api/health

echo "2. 混合操作测试 (100次请求，5个并发):"
# 创建POST数据文件
echo "username=concurrent_user&email=concurrent@demo.com&phone=13800000000&password=test123" > post_data.txt

ab -n 100 -c 5 -p post_data.txt -T application/x-www-form-urlencoded http://localhost:8080/api/users

echo "=== 并发测试完成 ==="
```

### 阶段六：故障恢复验证

#### ✅ 数据库故障模拟
```bash
#!/bin/bash
echo "=== 数据库故障恢复测试 ==="

# 1. 停止数据库
echo "1. 停止Oracle数据库..."
sudo systemctl stop oracle-xe

# 2. 测试应用响应
echo "2. 测试应用响应 (应该显示数据库连接错误):"
curl -w "\n状态码: %{http_code}\n" http://localhost:8080/api/health

# 3. 重启数据库
echo "3. 重启Oracle数据库..."
sudo systemctl start oracle-xe

# 4. 等待数据库完全启动
echo "4. 等待数据库启动..."
sleep 30

# 5. 测试应用恢复
echo "5. 测试应用恢复:"
for i in {1..5}; do
    echo "第${i}次尝试..."
    response=$(curl -s http://localhost:8080/api/health | jq -r '.status // "ERROR"')
    if [ "$response" = "UP" ]; then
        echo "✅ 应用恢复正常"
        break
    else
        echo "❌ 应用尚未恢复，等待..."
        sleep 5
    fi
done

echo "=== 故障恢复测试完成 ==="
```

#### ✅ 应用故障恢复测试
```bash
#!/bin/bash
echo "=== 应用故障恢复测试 ==="

# 1. 停止应用
echo "1. 停止应用服务..."
sudo systemctl stop readwrite-demo.service

# 2. 测试访问
echo "2. 测试访问 (应该连接失败):"
curl -w "\n连接状态: %{http_code}\n" http://localhost:8080/api/health || echo "连接失败 (预期行为)"

# 3. 重启应用
echo "3. 重启应用服务..."
sudo systemctl start readwrite-demo.service

# 4. 等待应用启动
echo "4. 等待应用启动..."
for i in {1..12}; do
    echo "第${i}次检查..."
    if systemctl is-active --quiet readwrite-demo.service; then
        sleep 5
        response=$(curl -s http://localhost:8080/api/health 2>/dev/null | jq -r '.status // "ERROR"' 2>/dev/null)
        if [ "$response" = "UP" ]; then
            echo "✅ 应用恢复正常"
            break
        fi
    fi
    echo "等待中..."
    sleep 10
done

echo "=== 应用故障恢复测试完成 ==="
```

## 🔍 问题排查指南

### 常见问题及解决方案

#### 1. 数据库连接问题
**症状**: 应用启动失败，提示数据库连接错误

**排查步骤**:
```bash
# 检查Oracle服务状态
sudo systemctl status oracle-xe

# 检查监听器状态
lsnrctl status

# 测试数据库连接
sqlplus MASTER_USER/master_password@localhost:1521/XEPDB1

# 检查网络连通性
telnet localhost 1521
```

#### 2. 端口占用问题
**症状**: 应用启动失败，提示端口被占用

**解决方案**:
```bash
# 查看端口占用
netstat -tulpn | grep 8080

# 杀死占用进程
sudo kill -9 [PID]

# 或更改应用端口
# 修改 application.yml 中的 server.port
```

#### 3. 内存不足问题
**症状**: 应用启动缓慢或OOM错误

**解决方案**:
```bash
# 检查内存使用
free -h

# 调整JVM参数
export JAVA_OPTS="-Xms256m -Xmx512m"

# 或修改 systemd 服务文件
sudo systemctl edit readwrite-demo.service
```

#### 4. 读写分离不生效
**症状**: 所有操作都使用同一个数据源

**排查步骤**:
```bash
# 检查AOP配置
journalctl -u readwrite-demo.service | grep -i aop

# 检查数据源切换日志
journalctl -u readwrite-demo.service | grep -E "(使用主库|使用从库)"

# 检查注解配置
journalctl -u readwrite-demo.service | grep "@MasterDB"
```

## 📊 监控指标

### 关键监控指标

1. **应用指标**
   - 响应时间
   - QPS (每秒查询数)
   - 错误率
   - JVM内存使用

2. **数据库指标**
   - 连接池使用率
   - 慢查询数量
   - 读写分离比例
   - 数据库响应时间

3. **系统指标**
   - CPU使用率
   - 内存使用率
   - 磁盘I/O
   - 网络I/O

### 监控命令
```bash
# 实时监控应用日志
tail -f /var/log/readwrite-demo/readwrite-demo.log

# 监控系统资源
htop

# 监控数据库
sqlplus MASTER_USER/master_password@localhost:1521/XEPDB1 <<< "SELECT * FROM v\$session WHERE username IN ('MASTER_USER','SLAVE_USER');"

# 监控网络连接
netstat -an | grep :8080
```

## ✅ 验收标准

### 功能验收
- [ ] 所有REST API接口正常响应
- [ ] 读操作默认使用从库
- [ ] 写操作自动使用主库
- [ ] @MasterDB注解强制主库生效
- [ ] 支付相关操作全部使用主库
- [ ] 数据一致性保证

### 性能验收
- [ ] 单个请求响应时间 < 200ms
- [ ] 并发100用户，响应时间 < 500ms
- [ ] 系统资源使用合理(CPU < 80%, 内存 < 80%)
- [ ] 数据库连接池使用正常

### 可靠性验收
- [ ] 应用自动重启功能正常
- [ ] 数据库断连后自动恢复
- [ ] 日志记录完整
- [ ] 监控指标正常

---

**部署完成后，请逐一执行上述验证步骤，确保系统稳定运行。如遇问题，请参考问题排查指南或联系技术支持。**