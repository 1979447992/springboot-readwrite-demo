-- Oracle数据库初始化脚本
-- 创建读写分离demo的用户和测试表

-- 创建主库用户（写操作）
CREATE USER MASTER_USER IDENTIFIED BY master123
DEFAULT TABLESPACE USERS
TEMPORARY TABLESPACE TEMP;

-- 授权主库用户
GRANT CONNECT, RESOURCE, DBA TO MASTER_USER;
GRANT CREATE SESSION TO MASTER_USER;
GRANT CREATE TABLE TO MASTER_USER;
GRANT CREATE SEQUENCE TO MASTER_USER;
GRANT CREATE VIEW TO MASTER_USER;
GRANT UNLIMITED TABLESPACE TO MASTER_USER;

-- 创建从库用户（读操作）
CREATE USER SLAVE_USER IDENTIFIED BY slave123
DEFAULT TABLESPACE USERS
TEMPORARY TABLESPACE TEMP;

-- 授权从库用户
GRANT CONNECT, RESOURCE TO SLAVE_USER;
GRANT CREATE SESSION TO SLAVE_USER;
GRANT CREATE TABLE TO SLAVE_USER;
GRANT CREATE SEQUENCE TO SLAVE_USER;
GRANT CREATE VIEW TO SLAVE_USER;
GRANT UNLIMITED TABLESPACE TO SLAVE_USER;

-- 在主库用户下创建测试表
CONNECT MASTER_USER/master123@XEPDB1;

CREATE TABLE demo_user (
    id NUMBER(19) PRIMARY KEY,
    username VARCHAR2(50) NOT NULL,
    email VARCHAR2(100),
    age NUMBER(3),
    created_time DATE DEFAULT SYSDATE,
    updated_time DATE DEFAULT SYSDATE
);

CREATE SEQUENCE user_seq START WITH 1 INCREMENT BY 1;

-- 插入测试数据
INSERT INTO demo_user (id, username, email, age) VALUES (user_seq.nextval, 'master_user1', 'master1@test.com', 25);
INSERT INTO demo_user (id, username, email, age) VALUES (user_seq.nextval, 'master_user2', 'master2@test.com', 30);
INSERT INTO demo_user (id, username, email, age) VALUES (user_seq.nextval, 'master_user3', 'master3@test.com', 28);

-- 在从库用户下创建相同结构的表（模拟同步）
CONNECT SLAVE_USER/slave123@XEPDB1;

CREATE TABLE demo_user (
    id NUMBER(19) PRIMARY KEY,
    username VARCHAR2(50) NOT NULL,
    email VARCHAR2(100),
    age NUMBER(3),
    created_time DATE DEFAULT SYSDATE,
    updated_time DATE DEFAULT SYSDATE
);

CREATE SEQUENCE user_seq START WITH 1 INCREMENT BY 1;

-- 插入测试数据（模拟从主库同步的数据）
INSERT INTO demo_user (id, username, email, age) VALUES (user_seq.nextval, 'slave_user1', 'slave1@test.com', 26);
INSERT INTO demo_user (id, username, email, age) VALUES (user_seq.nextval, 'slave_user2', 'slave2@test.com', 31);
INSERT INTO demo_user (id, username, email, age) VALUES (user_seq.nextval, 'slave_user3', 'slave3@test.com', 29);

-- 授权从库用户可以读取主库用户的表（可选，用于更真实的主从模拟）
GRANT SELECT ON MASTER_USER.demo_user TO SLAVE_USER;

COMMIT;