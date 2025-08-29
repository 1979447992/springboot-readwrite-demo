#!/bin/bash

echo "=========================================="
echo "2核4GB云服务器验证脚本"
echo "=========================================="

# 1. 检查系统资源
echo "1. 系统资源检查"
echo "CPU核数: $(nproc)"
echo "总内存: $(free -h | awk 'NR==2{printf "%.1fGB", $2/1024/1024/1024}')"
echo "可用内存: $(free -h | awk 'NR==2{printf "%.1fGB", $7/1024/1024/1024}')"
echo "磁盘空间: $(df -h / | awk 'NR==2{print $4}')"

# 2. 检查内存是否足够
total_mem=$(free -m | awk 'NR==2{print $2}')
if [ $total_mem -lt 3500 ]; then
    echo "⚠️  警告: 内存可能不足4GB，建议升级配置"
    echo "当前内存: ${total_mem}MB"
    echo "建议配置: 4GB (4096MB)"
else
    echo "✅ 内存充足: ${total_mem}MB"
fi

# 3. 创建swap文件（如果内存不足）
if [ $total_mem -lt 4000 ]; then
    echo "3. 创建swap文件补充内存..."
    if [ ! -f /swapfile ]; then
        sudo fallocate -l 2G /swapfile
        sudo chmod 600 /swapfile
        sudo mkswap /swapfile
        sudo swapon /swapfile
        echo "/swapfile none swap sw 0 0" | sudo tee -a /etc/fstab
        echo "✅ 2GB swap文件已创建"
    else
        echo "✅ swap文件已存在"
    fi
fi

# 4. 优化系统参数
echo "4. 优化系统参数..."
echo "vm.swappiness = 10" | sudo tee -a /etc/sysctl.conf
echo "vm.vfs_cache_pressure = 50" | sudo tee -a /etc/sysctl.conf
sudo sysctl -p

# 5. 设置Java内存参数
echo "5. 配置Java内存参数..."
cat > /tmp/java-opts.conf << 'EOF'
export JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError"
export ORACLE_SGA_MAX_SIZE=500M  
export ORACLE_PGA_AGGREGATE_TARGET=200M
EOF

echo "请将以下内容添加到 ~/.bashrc:"
echo "=========================================="
cat /tmp/java-opts.conf
echo "=========================================="

# 6. 检查网络和防火墙
echo "6. 网络配置检查..."
if command -v ufw &> /dev/null; then
    echo "防火墙状态: $(sudo ufw status | head -1)"
else
    echo "未安装ufw防火墙"
fi

# 7. 预估资源使用
echo "7. 预估资源使用情况:"
echo "┌─────────────────┬──────────┐"
echo "│ 组件            │ 内存占用  │"
echo "├─────────────────┼──────────┤"
echo "│ Ubuntu系统      │ ~800MB   │"
echo "│ Oracle XE       │ ~1.2GB   │" 
echo "│ Spring Boot应用 │ ~512MB   │"
echo "│ 系统缓存        │ ~300MB   │"
echo "│ 预留            │ ~200MB   │"
echo "├─────────────────┼──────────┤"
echo "│ 总计            │ ~3.0GB   │"
echo "└─────────────────┴──────────┘"

if [ $total_mem -ge 3500 ]; then
    echo "✅ 4GB内存可以正常运行此项目"
else
    echo "⚠️  建议升级到4GB内存以获得最佳性能"
fi

echo "=========================================="
echo "资源检查完成！"
echo "如需继续部署，请运行: ./deploy.sh"
echo "=========================================="