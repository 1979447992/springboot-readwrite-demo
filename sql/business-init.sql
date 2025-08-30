-- Business数据库初始化脚本
-- 创建订单管理相关表

USE business_demo;

-- 安全地创建业务用户（如果不存在）
CREATE USER IF NOT EXISTS 'business_user'@'%' IDENTIFIED BY 'business123';
GRANT ALL PRIVILEGES ON business_demo.* TO 'business_user'@'%';
FLUSH PRIVILEGES;

-- 产品表
CREATE TABLE IF NOT EXISTS `product` (
    `id` BIGINT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL COMMENT '产品名称',
    `category` VARCHAR(50) NOT NULL COMMENT '产品分类',
    `price` DECIMAL(10,2) NOT NULL COMMENT '产品价格',
    `stock` INT DEFAULT 0 COMMENT '库存数量',
    `description` TEXT COMMENT '产品描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-上架，0-下架',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='产品表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order_info` (
    `id` BIGINT PRIMARY KEY,
    `order_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '产品ID',
    `quantity` INT NOT NULL COMMENT '购买数量',
    `unit_price` DECIMAL(10,2) NOT NULL COMMENT '单价',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '订单状态：PENDING-待处理,PAID-已支付,SHIPPED-已发货,COMPLETED-已完成,CANCELLED-已取消',
    `remark` VARCHAR(500) COMMENT '备注',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) COMMENT='订单表';

-- 订单日志表
CREATE TABLE IF NOT EXISTS `order_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `action` VARCHAR(50) NOT NULL COMMENT '操作类型',
    `old_status` VARCHAR(20) COMMENT '原状态',
    `new_status` VARCHAR(20) COMMENT '新状态',
    `operator` VARCHAR(50) COMMENT '操作人',
    `remark` VARCHAR(500) COMMENT '备注',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id)
) COMMENT='订单日志表';

-- 安全地初始化产品数据
INSERT IGNORE INTO `product` (`id`, `name`, `category`, `price`, `stock`, `description`) VALUES
(2001, 'iPhone 15 Pro', '手机', 7999.00, 100, '苹果最新旗舰手机'),
(2002, 'MacBook Air M3', '笔记本', 9499.00, 50, 'Apple M3芯片笔记本电脑'),
(2003, 'AirPods Pro 2', '耳机', 1499.00, 200, '苹果无线降噪耳机'),
(2004, 'iPad Air', '平板', 4799.00, 80, '10.9英寸iPad Air'),
(2005, 'Apple Watch Series 9', '手表', 2999.00, 120, '苹果智能手表');

-- 安全地初始化订单数据
INSERT IGNORE INTO `order_info` (`id`, `order_no`, `user_id`, `product_id`, `quantity`, `unit_price`, `total_amount`, `status`) VALUES
(3001, 'ORD202508300001', 1001, 2001, 1, 7999.00, 7999.00, 'COMPLETED'),
(3002, 'ORD202508300002', 1002, 2002, 1, 9499.00, 9499.00, 'PAID'),
(3003, 'ORD202508300003', 1003, 2003, 2, 1499.00, 2998.00, 'SHIPPED'),
(3004, 'ORD202508300004', 1001, 2004, 1, 4799.00, 4799.00, 'PENDING');

-- 安全地初始化订单日志
INSERT IGNORE INTO `order_log` (`order_id`, `action`, `old_status`, `new_status`, `operator`, `remark`) VALUES
(3001, 'CREATE', NULL, 'PENDING', 'system', '订单创建'),
(3001, 'PAY', 'PENDING', 'PAID', 'user', '用户支付'),
(3001, 'SHIP', 'PAID', 'SHIPPED', 'admin', '商品发货'),
(3001, 'COMPLETE', 'SHIPPED', 'COMPLETED', 'system', '订单完成'),
(3002, 'CREATE', NULL, 'PENDING', 'system', '订单创建'),
(3002, 'PAY', 'PENDING', 'PAID', 'user', '用户支付');

COMMIT;
