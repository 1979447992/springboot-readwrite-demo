#!/bin/bash

echo "=========================================="
echo "ShardingSphere 读写分离验证脚本"
echo "=========================================="

BASE_URL="http://localhost:8080/api"

echo "1. 启动应用验证..."
response=$(curl -s "${BASE_URL}/health" | jq -r '.status // "ERROR"' 2>/dev/null)
if [ "$response" = "UP" ]; then
    echo "✅ 应用启动成功"
else
    echo "❌ 应用启动失败，请检查日志"
    exit 1
fi

echo -e "\n2. 读写分离自动路由验证..."

# 清理日志
echo "清理应用日志..."
if [ -f "/var/log/readwrite-demo/readwrite-demo.log" ]; then
    sudo truncate -s 0 /var/log/readwrite-demo/readwrite-demo.log
fi

echo "执行读操作（应该路由到从库）..."
curl -s "${BASE_URL}/users" > /dev/null
curl -s "${BASE_URL}/bills" > /dev/null

echo "执行写操作（应该路由到主库）..."
curl -s -X POST "${BASE_URL}/users" \
    -d "username=test_$(date +%s)&email=test$(date +%s)@demo.com&phone=138$(date +%s | tail -c 9)&password=test123" > /dev/null

echo "执行MyBatis Plus查询（应该路由到从库）..."
curl -s "${BASE_URL}/payments" > /dev/null

echo "执行MyBatis Plus更新（应该路由到主库）..."
curl -s -X POST "${BASE_URL}/bills" \
    -d "userId=1001&title=SS测试$(date +%s)&amount=100.00&type=1&remark=ShardingSphere测试" > /dev/null

echo -e "\n3. 检查ShardingSphere路由日志..."
echo "查看最近1分钟的SQL路由日志："

# 查看应用日志中的SQL执行情况
if command -v journalctl &> /dev/null; then
    echo "=== 从systemd日志查看SQL执行 ==="
    journalctl -u readwrite-demo.service --since="1 minute ago" | grep -E "(Actual SQL|Route|master|slave)" | tail -10
fi

if [ -f "/var/log/readwrite-demo/readwrite-demo.log" ]; then
    echo "=== 从应用日志查看SQL执行 ==="
    tail -20 /var/log/readwrite-demo/readwrite-demo.log | grep -E "(Actual SQL|Route|master|slave)"
fi

echo -e "\n4. 强制主库路由验证..."
echo "执行强制主库查询..."

# 这里需要有一个专门的测试接口来验证强制主库功能
# 临时创建测试数据
test_user_id=1001
echo "创建测试支付记录..."
payment_response=$(curl -s -X POST "${BASE_URL}/payments" \
    -d "userId=${test_user_id}&amount=0.01&paymentMethod=测试&description=强制主库测试")

if echo "$payment_response" | grep -q "ORDER"; then
    echo "✅ 测试支付记录创建成功"
    # 提取订单号进行测试
    order_no=$(echo "$payment_response" | jq -r '.orderNo // "ORDER_TEST"' 2>/dev/null)
    echo "测试强制主库查询..."
    curl -s "${BASE_URL}/payments/order/${order_no}" > /dev/null
else
    echo "⚠️  测试支付记录创建可能失败，继续其他测试..."
fi

echo -e "\n5. 性能对比测试..."
echo "测试读操作性能（从库）..."
read_start=$(date +%s%N)
for i in {1..10}; do
    curl -s "${BASE_URL}/users" > /dev/null
done
read_end=$(date +%s%N)
read_time=$((($read_end - $read_start) / 1000000))
echo "10次读操作耗时: ${read_time}ms"

echo "测试写操作性能（主库）..."
write_start=$(date +%s%N)
for i in {1..5}; do
    curl -s -X POST "${BASE_URL}/bills" \
        -d "userId=1001&title=性能测试${i}&amount=1.00&type=1&remark=性能测试" > /dev/null
done
write_end=$(date +%s%N)
write_time=$((($write_end - $write_start) / 1000000))
echo "5次写操作耗时: ${write_time}ms"

echo -e "\n6. 数据一致性验证..."
echo "在主库创建数据，检查从库是否能查询到..."

# 创建测试数据
echo "在主库创建测试用户..."
create_response=$(curl -s -X POST "${BASE_URL}/users" \
    -d "username=consistency_test_$(date +%s)&email=consistency@test.com&phone=13900000001&password=test123")

if echo "$create_response" | grep -q "consistency_test"; then
    echo "✅ 主库写入成功"
    
    # 等待一下，然后从从库查询
    sleep 2
    echo "从库查询验证..."
    query_response=$(curl -s "${BASE_URL}/users")
    if echo "$query_response" | grep -q "consistency_test"; then
        echo "✅ 从库数据同步正常"
    else
        echo "⚠️  从库可能未同步或同步延迟"
    fi
else
    echo "❌ 主库写入失败"
fi

echo -e "\n=========================================="
echo "验证完成！"
echo "请检查上述输出结果："
echo "1. 读操作应该显示路由到从库的日志"
echo "2. 写操作应该显示路由到主库的日志" 
echo "3. 强制主库查询应该在主库执行"
echo "4. 数据一致性测试应该通过"
echo "=========================================="