-- Log Database Initialization Script
-- This database stores application logs and audit trails

-- Create application_logs table
CREATE TABLE IF NOT EXISTS application_logs (
    id SERIAL PRIMARY KEY,
    log_level VARCHAR(10) NOT NULL,
    logger_name VARCHAR(255),
    message TEXT NOT NULL,
    thread_name VARCHAR(100),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    exception_trace TEXT,
    user_id BIGINT,
    session_id VARCHAR(100),
    request_id VARCHAR(100)
);

-- Create audit_logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    id SERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(100),
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(255),
    resource_id VARCHAR(100),
    old_value JSONB,
    new_value JSONB,
    ip_address INET,
    user_agent TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'SUCCESS'
);

-- Create system_metrics table
CREATE TABLE IF NOT EXISTS system_metrics (
    id SERIAL PRIMARY KEY,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(15,4),
    metric_unit VARCHAR(20),
    tags JSONB,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample application logs
INSERT INTO application_logs (log_level, logger_name, message, thread_name, user_id, session_id) VALUES
('INFO', 'com.demo.readwrite.service.UserService', '用户登录成功', 'http-nio-8080-exec-1', 1001, 'sess_001'),
('DEBUG', 'com.demo.readwrite.config.DataSourceConfig', '数据源路由到master库', 'http-nio-8080-exec-2', NULL, NULL),
('WARN', 'com.demo.readwrite.service.ConfigService', '配置缓存未命中', 'http-nio-8080-exec-3', NULL, NULL),
('ERROR', 'com.demo.readwrite.controller.UserController', '用户不存在', 'http-nio-8080-exec-4', NULL, 'sess_002'),
('INFO', 'com.demo.readwrite.service.LogService', '审计日志记录完成', 'async-1', 1002, 'sess_003');

-- Insert sample audit logs
INSERT INTO audit_logs (user_id, username, action, resource, resource_id, old_value, new_value, ip_address) VALUES
(1001, 'admin', 'CREATE', 'user', '1001', NULL, '{"name":"张三","email":"zhangsan@demo.com"}', '192.168.1.100'),
(1001, 'admin', 'UPDATE', 'user', '1002', '{"status":"inactive"}', '{"status":"active"}', '192.168.1.100'),
(1002, 'user1', 'DELETE', 'config', 'cache_setting', '{"enable_cache":"true"}', NULL, '192.168.1.101'),
(1002, 'user1', 'VIEW', 'report', 'monthly_stats', NULL, NULL, '192.168.1.101');

-- Insert sample system metrics
INSERT INTO system_metrics (metric_name, metric_value, metric_unit, tags) VALUES
('cpu_usage', 75.5, 'percent', '{"server":"app-01","region":"us-east"}'),
('memory_usage', 8192, 'MB', '{"server":"app-01","region":"us-east"}'),
('db_connection_pool', 15, 'count', '{"database":"master","pool":"HikariCP"}'),
('response_time', 245.8, 'ms', '{"endpoint":"/api/users","method":"GET"}'),
('error_rate', 0.05, 'percent', '{"service":"user-service","environment":"prod"}');

-- Create indexes for better query performance
CREATE INDEX idx_app_logs_timestamp ON application_logs(timestamp);
CREATE INDEX idx_app_logs_level ON application_logs(log_level);
CREATE INDEX idx_app_logs_user ON application_logs(user_id);

CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource);

CREATE INDEX idx_metrics_timestamp ON system_metrics(timestamp);
CREATE INDEX idx_metrics_name ON system_metrics(metric_name);