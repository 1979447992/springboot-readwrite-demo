#!/bin/bash

# Oracle数据库部署和配置脚本

set -e

echo "=========================================="
echo "Oracle数据库配置脚本"
echo "=========================================="

# 配置变量
ORACLE_HOST=${ORACLE_HOST:-localhost}
ORACLE_PORT=${ORACLE_PORT:-1521}
ORACLE_SID=${ORACLE_SID:-XE}
ORACLE_PDB=${ORACLE_PDB:-XEPDB1}
SYS_PASSWORD=${SYS_PASSWORD:-OraclePassword123}
MASTER_PASSWORD=${MASTER_PASSWORD:-master_password}
SLAVE_PASSWORD=${SLAVE_PASSWORD:-slave_password}

echo "步骤 1: 检查Oracle连接..."
if ! sqlplus -s sys/${SYS_PASSWORD}@${ORACLE_HOST}:${ORACLE_PORT}/${ORACLE_SID} as sysdba <<< "SELECT 1 FROM DUAL;" > /dev/null 2>&1; then
    echo "错误: 无法连接到Oracle数据库"
    echo "请确保Oracle数据库正在运行并且连接参数正确"
    exit 1
fi
echo "Oracle连接成功！"

echo "步骤 2: 创建数据库用户..."
sqlplus -s sys/${SYS_PASSWORD}@${ORACLE_HOST}:${ORACLE_PORT}/${ORACLE_SID} as sysdba << EOF
ALTER SESSION SET CONTAINER = ${ORACLE_PDB};

-- 删除已存在的用户（如果存在）
DECLARE
    user_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM dba_users WHERE username = 'MASTER_USER';
    IF user_count > 0 THEN
        EXECUTE IMMEDIATE 'DROP USER MASTER_USER CASCADE';
    END IF;
    
    SELECT COUNT(*) INTO user_count FROM dba_users WHERE username = 'SLAVE_USER';
    IF user_count > 0 THEN
        EXECUTE IMMEDIATE 'DROP USER SLAVE_USER CASCADE';
    END IF;
END;
/

-- 创建主库用户
CREATE USER MASTER_USER IDENTIFIED BY ${MASTER_PASSWORD}
DEFAULT TABLESPACE USERS
TEMPORARY TABLESPACE TEMP
QUOTA UNLIMITED ON USERS;

-- 创建从库用户
CREATE USER SLAVE_USER IDENTIFIED BY ${SLAVE_PASSWORD}
DEFAULT TABLESPACE USERS
TEMPORARY TABLESPACE TEMP
QUOTA UNLIMITED ON USERS;

-- 授权主库用户
GRANT CONNECT TO MASTER_USER;
GRANT RESOURCE TO MASTER_USER;
GRANT CREATE SESSION TO MASTER_USER;
GRANT CREATE TABLE TO MASTER_USER;
GRANT CREATE SEQUENCE TO MASTER_USER;
GRANT CREATE TRIGGER TO MASTER_USER;

-- 授权从库用户
GRANT CONNECT TO SLAVE_USER;
GRANT CREATE SESSION TO SLAVE_USER;

COMMIT;
EXIT;
EOF

if [ $? -eq 0 ]; then
    echo "数据库用户创建成功！"
else
    echo "数据库用户创建失败！"
    exit 1
fi

echo "步骤 3: 创建表结构..."
sqlplus -s MASTER_USER/${MASTER_PASSWORD}@${ORACLE_HOST}:${ORACLE_PORT}/${ORACLE_PDB} << EOF
@schema.sql
EXIT;
EOF

if [ $? -eq 0 ]; then
    echo "表结构创建成功！"
else
    echo "表结构创建失败！"
    exit 1
fi

echo "步骤 4: 插入测试数据..."
sqlplus -s MASTER_USER/${MASTER_PASSWORD}@${ORACLE_HOST}:${ORACLE_PORT}/${ORACLE_PDB} << EOF
@data.sql
EXIT;
EOF

if [ $? -eq 0 ]; then
    echo "测试数据插入成功！"
else
    echo "测试数据插入失败！"
    exit 1
fi

echo "步骤 5: 配置从库用户权限..."
sqlplus -s sys/${SYS_PASSWORD}@${ORACLE_HOST}:${ORACLE_PORT}/${ORACLE_SID} as sysdba << EOF
ALTER SESSION SET CONTAINER = ${ORACLE_PDB};

-- 授予从库用户查询权限
GRANT SELECT ON MASTER_USER.users TO SLAVE_USER;
GRANT SELECT ON MASTER_USER.payments TO SLAVE_USER;
GRANT SELECT ON MASTER_USER.bills TO SLAVE_USER;

-- 创建同义词
CREATE OR REPLACE SYNONYM SLAVE_USER.users FOR MASTER_USER.users;
CREATE OR REPLACE SYNONYM SLAVE_USER.payments FOR MASTER_USER.payments;
CREATE OR REPLACE SYNONYM SLAVE_USER.bills FOR MASTER_USER.bills;

COMMIT;
EXIT;
EOF

if [ $? -eq 0 ]; then
    echo "从库用户权限配置成功！"
else
    echo "从库用户权限配置失败！"
    exit 1
fi

echo "步骤 6: 验证配置..."
echo "验证主库用户连接..."
if sqlplus -s MASTER_USER/${MASTER_PASSWORD}@${ORACLE_HOST}:${ORACLE_PORT}/${ORACLE_PDB} <<< "SELECT COUNT(*) FROM users;" > /dev/null 2>&1; then
    echo "主库用户连接验证成功！"
else
    echo "主库用户连接验证失败！"
    exit 1
fi

echo "验证从库用户连接..."
if sqlplus -s SLAVE_USER/${SLAVE_PASSWORD}@${ORACLE_HOST}:${ORACLE_PORT}/${ORACLE_PDB} <<< "SELECT COUNT(*) FROM users;" > /dev/null 2>&1; then
    echo "从库用户连接验证成功！"
else
    echo "从库用户连接验证失败！"
    exit 1
fi

echo "=========================================="
echo "Oracle数据库配置完成！"
echo ""
echo "数据库信息："
echo "主库用户: MASTER_USER"
echo "从库用户: SLAVE_USER"
echo "连接字符串: ${ORACLE_HOST}:${ORACLE_PORT}/${ORACLE_PDB}"
echo ""
echo "可以使用以下命令测试连接："
echo "sqlplus MASTER_USER/${MASTER_PASSWORD}@${ORACLE_HOST}:${ORACLE_PORT}/${ORACLE_PDB}"
echo "sqlplus SLAVE_USER/${SLAVE_PASSWORD}@${ORACLE_HOST}:${ORACLE_PORT}/${ORACLE_PDB}"
echo "=========================================="