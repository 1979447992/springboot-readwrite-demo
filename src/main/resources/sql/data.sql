-- Oracle数据库测试数据初始化脚本
-- 请在创建完表结构后执行此脚本

-- 1. 插入测试用户数据
INSERT INTO users (id, username, email, phone, password, status, create_time, update_time, deleted) VALUES
(1001, 'admin', 'admin@demo.com', '13800000001', 'admin123', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO users (id, username, email, phone, password, status, create_time, update_time, deleted) VALUES
(1002, 'user1', 'user1@demo.com', '13800000002', 'user123', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO users (id, username, email, phone, password, status, create_time, update_time, deleted) VALUES
(1003, 'user2', 'user2@demo.com', '13800000003', 'user123', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO users (id, username, email, phone, password, status, create_time, update_time, deleted) VALUES
(1004, 'testuser', 'test@demo.com', '13800000004', 'test123', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO users (id, username, email, phone, password, status, create_time, update_time, deleted) VALUES
(1005, 'demouser', 'demo@demo.com', '13800000005', 'demo123', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 2. 插入测试支付数据
INSERT INTO payments (id, user_id, order_no, amount, payment_method, status, description, create_time, update_time, deleted) VALUES
(2001, 1001, 'ORDER_1700000001_ABC123', 99.99, '微信支付', 1, '购买VIP会员', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO payments (id, user_id, order_no, amount, payment_method, status, description, create_time, update_time, deleted) VALUES
(2002, 1002, 'ORDER_1700000002_DEF456', 299.00, '支付宝', 0, '购买课程', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO payments (id, user_id, order_no, amount, payment_method, status, description, create_time, update_time, deleted) VALUES
(2003, 1002, 'ORDER_1700000003_GHI789', 59.90, '银行卡', 1, '购买商品', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO payments (id, user_id, order_no, amount, payment_method, status, description, create_time, update_time, deleted) VALUES
(2004, 1003, 'ORDER_1700000004_JKL012', 129.00, '微信支付', 2, '购买服务', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO payments (id, user_id, order_no, amount, payment_method, status, description, create_time, update_time, deleted) VALUES
(2005, 1003, 'ORDER_1700000005_MNO345', 199.99, '支付宝', 3, '购买产品（已退款）', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO payments (id, user_id, order_no, amount, payment_method, status, description, create_time, update_time, deleted) VALUES
(2006, 1005, 'ORDER_1700000006_PQR678', 49.00, '微信支付', 1, '购买月卡', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 3. 插入测试账单数据
INSERT INTO bills (id, user_id, bill_no, title, amount, type, status, remark, create_time, update_time, deleted) VALUES
(3001, 1001, 'BILL_1700000001_A1B2C3', '工资收入', 8000.00, 1, 1, '11月工资', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO bills (id, user_id, bill_no, title, amount, type, status, remark, create_time, update_time, deleted) VALUES
(3002, 1001, 'BILL_1700000002_D4E5F6', '房租支出', 2500.00, 2, 1, '11月房租', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO bills (id, user_id, bill_no, title, amount, type, status, remark, create_time, update_time, deleted) VALUES
(3003, 1002, 'BILL_1700000003_G7H8I9', '兼职收入', 1200.00, 1, 1, '周末兼职', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO bills (id, user_id, bill_no, title, amount, type, status, remark, create_time, update_time, deleted) VALUES
(3004, 1002, 'BILL_1700000004_J1K2L3', '餐饮支出', 800.00, 2, 1, '11月餐饮费用', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO bills (id, user_id, bill_no, title, amount, type, status, remark, create_time, update_time, deleted) VALUES
(3005, 1003, 'BILL_1700000005_M4N5O6', '转账给朋友', 500.00, 3, 1, '借钱给小明', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO bills (id, user_id, bill_no, title, amount, type, status, remark, create_time, update_time, deleted) VALUES
(3006, 1003, 'BILL_1700000006_P7Q8R9', '网购支出', 299.99, 2, 1, '购买衣服', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO bills (id, user_id, bill_no, title, amount, type, status, remark, create_time, update_time, deleted) VALUES
(3007, 1005, 'BILL_1700000007_S1T2U3', '奖金收入', 2000.00, 1, 1, '季度奖金', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO bills (id, user_id, bill_no, title, amount, type, status, remark, create_time, update_time, deleted) VALUES
(3008, 1005, 'BILL_1700000008_V4W5X6', '交通支出', 150.00, 2, 1, '11月交通费', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 4. 更多测试数据用于验证读写分离
INSERT INTO users (id, username, email, phone, password, status, create_time, update_time, deleted) VALUES
(1006, 'readonly_user', 'readonly@demo.com', '13800000006', 'readonly123', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO payments (id, user_id, order_no, amount, payment_method, status, description, create_time, update_time, deleted) VALUES
(2007, 1006, 'ORDER_1700000007_TEST01', 1.00, '测试支付', 0, '测试读写分离', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO bills (id, user_id, bill_no, title, amount, type, status, remark, create_time, update_time, deleted) VALUES
(3009, 1006, 'BILL_1700000009_TEST01', '测试账单', 1.00, 1, 1, '用于测试读写分离功能', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

COMMIT;