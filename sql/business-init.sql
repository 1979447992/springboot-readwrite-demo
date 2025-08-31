-- 业务数据库初始化脚本

-- 使用数据库
USE business_demo;

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

-- 插入测试数据
INSERT IGNORE INTO payment (id, user_id, order_no, amount, payment_method, status, description) VALUES
(2001, 1001, 'ORDER_1700000001_ABC123', 99.99, '微信支付', 'SUCCESS', 'VIP会员'),
(2002, 1002, 'ORDER_1700000002_DEF456', 199.00, '支付宝', 'PENDING', '年费会员'),
(2003, 1001, 'ORDER_1700000003_GHI789', 49.99, '银行卡', 'SUCCESS', '月费会员');
