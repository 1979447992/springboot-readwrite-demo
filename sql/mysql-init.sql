-- MySQL数据库初始化脚本
-- 创建读写分离demo的用户和测试表

-- 创建主库用户（写操作）
CREATE USER IF NOT EXISTS 'master_user'@'%' IDENTIFIED BY 'master123';
GRANT ALL PRIVILEGES ON readwrite_demo.* TO 'master_user'@'%';

-- 创建从库用户（读操作）  
CREATE USER IF NOT EXISTS 'slave_user'@'%' IDENTIFIED BY 'slave123';
GRANT SELECT ON readwrite_demo.* TO 'slave_user'@'%';

-- 刷新权限
FLUSH PRIVILEGES;

-- 使用数据库
USE readwrite_demo;

-- 创建测试表
CREATE TABLE IF NOT EXISTS demo_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    password VARCHAR(100),
    age INT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建支付表
CREATE TABLE IF NOT EXISTS payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20),
    status VARCHAR(20) DEFAULT 'PENDING',
    description VARCHAR(200),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建账单表
CREATE TABLE IF NOT EXISTS bill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    type TINYINT DEFAULT 0 COMMENT '0-支出 1-收入',
    remark VARCHAR(200),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入测试数据
INSERT IGNORE INTO demo_user (id, username, email, phone, password, age) VALUES
(1001, 'admin', 'admin@demo.com', '13800000001', 'admin123', 25),
(1002, 'testuser', 'test@demo.com', '13800000002', 'password123', 30),
(1003, 'demo', 'demo@demo.com', '13800000003', 'demo123', 28);

INSERT IGNORE INTO payment (id, user_id, order_no, amount, payment_method, status, description) VALUES
(2001, 1001, 'ORDER_1700000001_ABC123', 99.99, '微信支付', 'SUCCESS', 'VIP会员'),
(2002, 1002, 'ORDER_1700000002_DEF456', 199.00, '支付宝', 'PENDING', '年费会员'),
(2003, 1001, 'ORDER_1700000003_GHI789', 49.99, '银行卡', 'SUCCESS', '月费会员');

INSERT IGNORE INTO bill (id, user_id, title, amount, type, remark) VALUES
(3001, 1001, '工资收入', 8000.00, 1, '11月工资'),
(3002, 1001, '餐饮支出', 50.00, 0, '午餐'),
(3003, 1002, '奖金收入', 2000.00, 1, '项目奖金'),
(3004, 1002, '交通支出', 30.00, 0, '地铁费用');

-- 授权从库用户访问这些表
GRANT SELECT ON readwrite_demo.demo_user TO 'slave_user'@'%';
GRANT SELECT ON readwrite_demo.payment TO 'slave_user'@'%';
GRANT SELECT ON readwrite_demo.bill TO 'slave_user'@'%';

FLUSH PRIVILEGES;
