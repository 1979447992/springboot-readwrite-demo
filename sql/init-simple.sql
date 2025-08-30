-- 创建用户表
CREATE DATABASE IF NOT EXISTS readwrite_demo;
USE readwrite_demo;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    age INT,
    phone VARCHAR(20),
    status INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_username (username),
    INDEX idx_email (email)
);

-- 插入测试数据
INSERT INTO users (username, email, age, phone) VALUES 
('admin', 'admin@demo.com', 30, '13800000001'),
('testuser', 'test@demo.com', 25, '13800000002');

-- 显示表结构
SHOW TABLES;
DESCRIBE users;
