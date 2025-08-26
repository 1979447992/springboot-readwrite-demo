#!/bin/bash

# Mixed Data Source Testing Script
# Tests read-write separation for core business + @DS for config/log databases

echo "========================================"
echo "🧪 混合数据源功能测试"
echo "========================================"
echo

BASE_URL="http://localhost:8080"

echo "📊 1. 获取系统状态"
curl -s "$BASE_URL/mixed/status" | json_pp
echo

echo "========================================"
echo "🏢 2. 测试主业务库读写分离"  
echo "========================================"

echo "📖 2.1 查询用户 (应该使用SLAVE库 localhost:5433)"
curl -s "$BASE_URL/mixed/users" | json_pp
echo

echo "✍️ 2.2 创建用户 (应该使用MASTER库 localhost:5432)"
curl -s -X POST "$BASE_URL/mixed/users" \
  -d "username=测试用户$(date +%s)" \
  -d "email=test$(date +%s)@demo.com" | json_pp
echo

echo "🔄 2.3 主从同步验证"
curl -s "$BASE_URL/mixed/sync-test" | json_pp
echo

echo "========================================"
echo "⚙️ 3. 测试配置库 @DS(\"config\") 操作"
echo "========================================"

echo "📋 3.1 查询所有配置 (localhost:5434)"
curl -s "$BASE_URL/mixed/config" | json_pp
echo

echo "🔍 3.2 查询单个配置"
curl -s "$BASE_URL/mixed/config/max_upload_size" | json_pp
echo

echo "📂 3.3 按组查询配置"
curl -s "$BASE_URL/mixed/config/group/system" | json_pp
echo

echo "➕ 3.4 创建新配置"
curl -s -X POST "$BASE_URL/mixed/config" \
  -d "key=test_config_$(date +%s)" \
  -d "value=test_value" \
  -d "description=测试配置" \
  -d "group=test" | json_pp
echo

echo "✏️ 3.5 更新配置"
curl -s -X PUT "$BASE_URL/mixed/config/enable_cache" \
  -d "value=false" | json_pp
echo

echo "🏷️ 3.6 查询配置分类"
curl -s "$BASE_URL/mixed/config/categories" | json_pp
echo

echo "========================================"
echo "📝 4. 测试日志库 @DS(\"log\") 操作"
echo "========================================"

echo "📔 4.1 写入应用日志 (localhost:5435)"
curl -s -X POST "$BASE_URL/mixed/log/app" \
  -d "level=INFO" \
  -d "logger=TestLogger" \
  -d "message=测试应用日志_$(date +%s)" \
  -d "thread=test-thread" \
  -d "userId=1001" \
  -d "sessionId=test_session" | json_pp
echo

echo "📋 4.2 写入审计日志"
curl -s -X POST "$BASE_URL/mixed/log/audit" \
  -d "userId=1001" \
  -d "username=test_user" \
  -d "action=TEST" \
  -d "resource=test_resource" \
  -d "resourceId=123" \
  -d "oldValue={\"status\":\"old\"}" \
  -d "newValue={\"status\":\"new\"}" \
  -d "ipAddress=192.168.1.100" | json_pp
echo

echo "📊 4.3 记录系统指标"
curl -s -X POST "$BASE_URL/mixed/log/metric" \
  -d "name=test_metric" \
  -d "value=99.5" \
  -d "unit=percent" \
  -d "tags=test_tag" | json_pp
echo

echo "🔍 4.4 查询应用日志"
curl -s "$BASE_URL/mixed/log/app?level=INFO&limit=5" | json_pp
echo

echo "🔍 4.5 查询审计日志"
curl -s "$BASE_URL/mixed/log/audit?userId=1001&limit=5" | json_pp
echo

echo "📈 4.6 查询系统指标"
curl -s "$BASE_URL/mixed/log/metric/test_metric?hours=24" | json_pp
echo

echo "========================================"
echo "✅ 测试完成！"
echo "========================================"
echo "验证点："
echo "1. 主业务库查询应显示使用SLAVE (localhost:5433)"
echo "2. 主业务库写入应显示使用MASTER (localhost:5432)"  
echo "3. 配置库操作应显示使用 @DS(\"config\") (localhost:5434)"
echo "4. 日志库操作应显示使用 @DS(\"log\") (localhost:5435)"
echo "5. 所有操作应返回真实数据库数据，而非模拟数据"