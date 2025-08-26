-- Config Database Initialization Script
-- This database stores application configuration data

-- Create config_settings table
CREATE TABLE IF NOT EXISTS config_settings (
    id SERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    description TEXT,
    config_group VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create config_categories table
CREATE TABLE IF NOT EXISTS config_categories (
    id SERIAL PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample configuration categories
INSERT INTO config_categories (category_name, description) VALUES
('system', '系统基础配置'),
('feature', '功能开关配置'),
('integration', '第三方集成配置'),
('performance', '性能优化配置');

-- Insert sample configuration data
INSERT INTO config_settings (config_key, config_value, description, config_group) VALUES
('max_upload_size', '10485760', '最大文件上传大小(字节)', 'system'),
('session_timeout', '1800', '会话超时时间(秒)', 'system'),
('enable_cache', 'true', '启用缓存功能', 'feature'),
('enable_notification', 'false', '启用消息通知', 'feature'),
('email_smtp_host', 'smtp.gmail.com', 'SMTP服务器地址', 'integration'),
('email_smtp_port', '587', 'SMTP服务器端口', 'integration'),
('db_pool_size', '20', '数据库连接池大小', 'performance'),
('cache_ttl', '3600', '缓存TTL(秒)', 'performance');

-- Create indexes for better query performance
CREATE INDEX idx_config_settings_key ON config_settings(config_key);
CREATE INDEX idx_config_settings_group ON config_settings(config_group);
CREATE INDEX idx_config_settings_active ON config_settings(is_active);