-- 测试数据初始化脚本

USE readwrite_demo;

-- 插入测试用户数据
INSERT INTO users (username, email, age, status) VALUES
('john_doe', 'john.doe@example.com', 25, 1),
('jane_smith', 'jane.smith@example.com', 30, 1),
('bob_wilson', 'bob.wilson@example.com', 28, 1),
('alice_brown', 'alice.brown@example.com', 32, 1),
('charlie_davis', 'charlie.davis@example.com', 27, 1),
('diana_miller', 'diana.miller@example.com', 29, 1),
('frank_garcia', 'frank.garcia@example.com', 35, 1),
('grace_martinez', 'grace.martinez@example.com', 26, 1),
('henry_anderson', 'henry.anderson@example.com', 31, 1),
('ivy_thomas', 'ivy.thomas@example.com', 24, 1);

-- 显示插入的数据
SELECT COUNT(*) as total_users FROM users;
SELECT * FROM users LIMIT 5;