#!/bin/bash

# Ubuntu服务器部署脚本
# 适用于Ubuntu 20.04+

set -e

echo "=========================================="
echo "Spring Boot读写分离Demo部署脚本"
echo "=========================================="

# 配置变量
APP_NAME="readwrite-demo"
APP_DIR="/opt/$APP_NAME"
LOG_DIR="/var/log/$APP_NAME"
SERVICE_NAME="$APP_NAME.service"
USER="appuser"

# 检查是否为root用户
if [ "$EUID" -ne 0 ]; then
    echo "请以root权限运行此脚本"
    exit 1
fi

echo "步骤 1: 更新系统包..."
apt-get update -y
apt-get upgrade -y

echo "步骤 2: 安装必要的软件..."
apt-get install -y curl wget git unzip openjdk-17-jdk maven docker.io docker-compose nginx

# 启动Docker服务
systemctl enable docker
systemctl start docker

echo "步骤 3: 创建应用用户..."
if ! id "$USER" &>/dev/null; then
    useradd -r -m -s /bin/bash $USER
    echo "用户 $USER 创建成功"
else
    echo "用户 $USER 已存在"
fi

echo "步骤 4: 创建应用目录..."
mkdir -p $APP_DIR
mkdir -p $LOG_DIR
chown -R $USER:$USER $APP_DIR
chown -R $USER:$USER $LOG_DIR

echo "步骤 5: 安装Oracle Instant Client..."
cd /tmp
wget https://download.oracle.com/otn_software/linux/instantclient/2113000/instantclient-basic-linux.x64-21.13.0.0.0dbru.zip
wget https://download.oracle.com/otn_software/linux/instantclient/2113000/instantclient-sqlplus-linux.x64-21.13.0.0.0dbru.zip

unzip instantclient-basic-linux.x64-21.13.0.0.0dbru.zip -d /opt/oracle/
unzip instantclient-sqlplus-linux.x64-21.13.0.0.0dbru.zip -d /opt/oracle/

# 设置Oracle环境变量
echo "export ORACLE_HOME=/opt/oracle/instantclient_21_13" >> /etc/environment
echo "export PATH=\$PATH:\$ORACLE_HOME" >> /etc/environment
echo "export LD_LIBRARY_PATH=\$ORACLE_HOME" >> /etc/environment

echo "步骤 6: 配置防火墙..."
#ufw allow 22     # SSH
#ufw allow 80     # HTTP
#ufw allow 443    # HTTPS
#ufw allow 8080   # Spring Boot
#ufw allow 1521   # Oracle
#ufw --force enable

echo "步骤 7: 创建systemd服务文件..."
cat > /etc/systemd/system/$SERVICE_NAME << EOF
[Unit]
Description=Spring Boot ReadWrite Demo Application
After=network.target

[Service]
Type=forking
User=$USER
Group=$USER
ExecStart=/usr/bin/java -jar $APP_DIR/app.jar
ExecStop=/bin/kill -TERM \$MAINPID
WorkingDirectory=$APP_DIR
Environment=JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
Environment=SPRING_PROFILES_ACTIVE=docker
StandardOutput=journal
StandardError=journal
SyslogIdentifier=$APP_NAME
KillMode=mixed
RestartSec=5
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF

echo "步骤 8: 配置Nginx..."
cat > /etc/nginx/sites-available/$APP_NAME << EOF
server {
    listen 80;
    server_name localhost;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    location /api/health {
        proxy_pass http://127.0.0.1:8080/api/health;
        access_log off;
    }
}
EOF

# 启用Nginx站点
ln -sf /etc/nginx/sites-available/$APP_NAME /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default
nginx -t && systemctl reload nginx

echo "步骤 9: 启动和启用服务..."
systemctl daemon-reload
systemctl enable $SERVICE_NAME
systemctl enable nginx

echo "步骤 10: 创建部署目录结构..."
mkdir -p $APP_DIR/{config,logs,backup}
chown -R $USER:$USER $APP_DIR

echo "=========================================="
echo "部署脚本执行完成！"
echo ""
echo "接下来的步骤："
echo "1. 将应用JAR文件复制到 $APP_DIR/app.jar"
echo "2. 配置Oracle数据库连接"
echo "3. 运行: systemctl start $SERVICE_NAME"
echo "4. 检查状态: systemctl status $SERVICE_NAME"
echo "5. 查看日志: journalctl -u $SERVICE_NAME -f"
echo ""
echo "应用将在 http://您的服务器IP 上运行"
echo "=========================================="