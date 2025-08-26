#!/bin/bash
# 从库设置脚本

set -e

echo "正在配置PostgreSQL从库..."

# 等待主库启动
sleep 30

# 停止PostgreSQL服务
pg_ctl -D "$PGDATA" stop -m fast || true

# 清空数据目录
rm -rf "$PGDATA"/*

# 从主库复制数据
pg_basebackup -h postgres-master -D "$PGDATA" -U replicator -v -P -W <<< "replica123"

# 创建从库配置
cat >> "$PGDATA/postgresql.conf" << EOL
# 从库配置
hot_standby = on
primary_conninfo = 'host=postgres-master port=5432 user=replicator password=replica123'
EOL

# 创建standby.signal文件标识为从库
touch "$PGDATA/standby.signal"

echo "从库配置完成，启动PostgreSQL..."

# 启动PostgreSQL
pg_ctl -D "$PGDATA" -l "$PGDATA/log" start