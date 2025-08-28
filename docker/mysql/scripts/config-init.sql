-- 配置数据库初始化脚本

CREATE DATABASE IF NOT EXISTS config_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE config_db;

-- 创建配置表
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 插入初始配置数据
INSERT INTO system_config (config_key, config_value, description) VALUES
('app.name', 'ReadWrite Demo', '应用名称'),
('app.version', '1.0.0', '应用版本'),
('db.pool.max', '20', '数据库连接池最大连接数'),
('cache.enabled', 'true', '是否启用缓存'),
('log.level', 'INFO', '日志级别');

-- 显示配置数据
SELECT COUNT(*) as total_configs FROM system_config;
SELECT * FROM system_config;