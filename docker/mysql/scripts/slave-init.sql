-- 从库初始化脚本

-- 创建业务数据库
CREATE DATABASE IF NOT EXISTS readwrite_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 配置主从复制
-- 注意：在实际生产中，需要先获取主库的MASTER_LOG_FILE和MASTER_LOG_POS
CHANGE MASTER TO 
    MASTER_HOST='mysql-master',
    MASTER_PORT=3306,
    MASTER_USER='replicator',
    MASTER_PASSWORD='replica123',
    MASTER_LOG_FILE='mysql-bin.000001',
    MASTER_LOG_POS=1,
    MASTER_CONNECT_RETRY=10,
    MASTER_RETRY_COUNT=86400;

-- 启动slave
START SLAVE;

-- 显示slave状态（用于验证）
-- SHOW SLAVE STATUS\G;