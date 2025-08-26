-- Oracle数据库用户和权限配置脚本
-- 请以系统管理员权限执行此脚本

-- 1. 创建主库用户（读写权限）
CREATE USER MASTER_USER IDENTIFIED BY master_password
DEFAULT TABLESPACE USERS
TEMPORARY TABLESPACE TEMP
QUOTA UNLIMITED ON USERS;

-- 2. 创建从库用户（只读权限）
CREATE USER SLAVE_USER IDENTIFIED BY slave_password
DEFAULT TABLESPACE USERS
TEMPORARY TABLESPACE TEMP
QUOTA UNLIMITED ON USERS;

-- 3. 为主库用户授权（完全权限）
GRANT CONNECT TO MASTER_USER;
GRANT RESOURCE TO MASTER_USER;
GRANT CREATE SESSION TO MASTER_USER;
GRANT CREATE TABLE TO MASTER_USER;
GRANT CREATE SEQUENCE TO MASTER_USER;
GRANT CREATE TRIGGER TO MASTER_USER;
GRANT CREATE PROCEDURE TO MASTER_USER;
GRANT CREATE VIEW TO MASTER_USER;
GRANT CREATE SYNONYM TO MASTER_USER;

-- 4. 为从库用户授权（只读权限）
GRANT CONNECT TO SLAVE_USER;
GRANT CREATE SESSION TO SLAVE_USER;

-- 5. 为从库用户授予表的查询权限
-- 注意：在实际的主从复制环境中，从库应该通过复制自动同步数据
-- 这里为了demo演示，我们授予从库用户对主库表的查询权限
GRANT SELECT ON MASTER_USER.users TO SLAVE_USER;
GRANT SELECT ON MASTER_USER.payments TO SLAVE_USER;
GRANT SELECT ON MASTER_USER.bills TO SLAVE_USER;

-- 6. 创建同义词，便于从库用户访问
CREATE OR REPLACE SYNONYM SLAVE_USER.users FOR MASTER_USER.users;
CREATE OR REPLACE SYNONYM SLAVE_USER.payments FOR MASTER_USER.payments;
CREATE OR REPLACE SYNONYM SLAVE_USER.bills FOR MASTER_USER.bills;

-- 7. 为演示需要，也给从库用户一些基本的查询系统表权限
GRANT SELECT ON sys.dual TO SLAVE_USER;

-- 8. 如果需要从库用户能够执行一些系统函数
GRANT EXECUTE ON sys.dbms_random TO SLAVE_USER;

COMMIT;

-- 验证用户创建成功
SELECT username, account_status, created FROM dba_users WHERE username IN ('MASTER_USER', 'SLAVE_USER');

-- 验证权限授予成功
SELECT grantee, privilege FROM dba_sys_privs WHERE grantee IN ('MASTER_USER', 'SLAVE_USER') ORDER BY grantee, privilege;

-- 注意事项：
-- 1. 在实际生产环境中，主从数据库通常是物理分离的不同服务器
-- 2. 从库通过Oracle DataGuard或其他复制技术同步主库数据
-- 3. 应用程序通过不同的连接配置连接到主库和从库
-- 4. 本demo为了简化，使用同一个数据库实例的不同用户来模拟主从库