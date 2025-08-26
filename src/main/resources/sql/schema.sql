-- Oracle数据库表结构初始化脚本
-- 适用于Oracle 11g及以上版本

-- 1. 创建用户表
CREATE TABLE users (
    id NUMBER(20) NOT NULL,
    username VARCHAR2(50) NOT NULL,
    email VARCHAR2(100),
    phone VARCHAR2(20),
    password VARCHAR2(255) NOT NULL,
    status NUMBER(1) DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted NUMBER(1) DEFAULT 0,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_phone UNIQUE (phone)
);

-- 2. 创建支付表
CREATE TABLE payments (
    id NUMBER(20) NOT NULL,
    user_id NUMBER(20) NOT NULL,
    order_no VARCHAR2(100) NOT NULL,
    amount NUMBER(10,2) NOT NULL,
    payment_method VARCHAR2(50),
    status NUMBER(1) DEFAULT 0,
    description VARCHAR2(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted NUMBER(1) DEFAULT 0,
    CONSTRAINT pk_payments PRIMARY KEY (id),
    CONSTRAINT uk_payments_order_no UNIQUE (order_no),
    CONSTRAINT fk_payments_user_id FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 3. 创建账单表
CREATE TABLE bills (
    id NUMBER(20) NOT NULL,
    user_id NUMBER(20) NOT NULL,
    bill_no VARCHAR2(100) NOT NULL,
    title VARCHAR2(200) NOT NULL,
    amount NUMBER(10,2) NOT NULL,
    type NUMBER(2) NOT NULL,
    status NUMBER(1) DEFAULT 1,
    remark VARCHAR2(1000),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted NUMBER(1) DEFAULT 0,
    CONSTRAINT pk_bills PRIMARY KEY (id),
    CONSTRAINT uk_bills_bill_no UNIQUE (bill_no),
    CONSTRAINT fk_bills_user_id FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 4. 创建索引
-- 用户表索引
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_create_time ON users(create_time);

-- 支付表索引
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_create_time ON payments(create_time);

-- 账单表索引
CREATE INDEX idx_bills_user_id ON bills(user_id);
CREATE INDEX idx_bills_type ON bills(type);
CREATE INDEX idx_bills_status ON bills(status);
CREATE INDEX idx_bills_create_time ON bills(create_time);

-- 5. 创建序列用于主键生成（可选，如果使用Oracle序列）
CREATE SEQUENCE seq_users_id START WITH 1000000000000000000 INCREMENT BY 1;
CREATE SEQUENCE seq_payments_id START WITH 1000000000000000000 INCREMENT BY 1;
CREATE SEQUENCE seq_bills_id START WITH 1000000000000000000 INCREMENT BY 1;

-- 6. 创建触发器，用于自动填充时间字段
CREATE OR REPLACE TRIGGER trg_users_update_time
    BEFORE UPDATE ON users
    FOR EACH ROW
BEGIN
    :NEW.update_time := CURRENT_TIMESTAMP;
END;
/

CREATE OR REPLACE TRIGGER trg_payments_update_time
    BEFORE UPDATE ON payments
    FOR EACH ROW
BEGIN
    :NEW.update_time := CURRENT_TIMESTAMP;
END;
/

CREATE OR REPLACE TRIGGER trg_bills_update_time
    BEFORE UPDATE ON bills
    FOR EACH ROW
BEGIN
    :NEW.update_time := CURRENT_TIMESTAMP;
END;
/

-- 7. 添加表注释
COMMENT ON TABLE users IS '用户表';
COMMENT ON COLUMN users.id IS '用户ID';
COMMENT ON COLUMN users.username IS '用户名';
COMMENT ON COLUMN users.email IS '邮箱';
COMMENT ON COLUMN users.phone IS '手机号';
COMMENT ON COLUMN users.password IS '密码';
COMMENT ON COLUMN users.status IS '状态: 0-禁用, 1-启用';
COMMENT ON COLUMN users.create_time IS '创建时间';
COMMENT ON COLUMN users.update_time IS '更新时间';
COMMENT ON COLUMN users.deleted IS '删除标记: 0-未删除, 1-已删除';

COMMENT ON TABLE payments IS '支付表';
COMMENT ON COLUMN payments.id IS '支付ID';
COMMENT ON COLUMN payments.user_id IS '用户ID';
COMMENT ON COLUMN payments.order_no IS '订单号';
COMMENT ON COLUMN payments.amount IS '支付金额';
COMMENT ON COLUMN payments.payment_method IS '支付方式';
COMMENT ON COLUMN payments.status IS '支付状态: 0-待支付, 1-已支付, 2-支付失败, 3-已退款';
COMMENT ON COLUMN payments.description IS '支付描述';
COMMENT ON COLUMN payments.create_time IS '创建时间';
COMMENT ON COLUMN payments.update_time IS '更新时间';
COMMENT ON COLUMN payments.deleted IS '删除标记: 0-未删除, 1-已删除';

COMMENT ON TABLE bills IS '账单表';
COMMENT ON COLUMN bills.id IS '账单ID';
COMMENT ON COLUMN bills.user_id IS '用户ID';
COMMENT ON COLUMN bills.bill_no IS '账单号';
COMMENT ON COLUMN bills.title IS '账单标题';
COMMENT ON COLUMN bills.amount IS '账单金额';
COMMENT ON COLUMN bills.type IS '账单类型: 1-收入, 2-支出, 3-转账';
COMMENT ON COLUMN bills.status IS '账单状态: 0-无效, 1-有效';
COMMENT ON COLUMN bills.remark IS '备注';
COMMENT ON COLUMN bills.create_time IS '创建时间';
COMMENT ON COLUMN bills.update_time IS '更新时间';
COMMENT ON COLUMN bills.deleted IS '删除标记: 0-未删除, 1-已删除';

COMMIT;