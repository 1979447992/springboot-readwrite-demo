package com.demo.readwrite.strategy;

import com.baomidou.dynamic.datasource.strategy.DynamicDataSourceStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 简单读写分离策略
 * 支持事务感知和强制主库访问
 */
@Component
@Slf4j
public class SimpleReadWriteStrategy implements DynamicDataSourceStrategy {

    // SQL类型缓存
    private static final ThreadLocal<SqlType> SQL_TYPE_HOLDER = new ThreadLocal<>();
    
    // 事务状态缓存
    private static final ThreadLocal<Boolean> TRANSACTION_HOLDER = new ThreadLocal<>();

    @Override
    public String determineKey(List<String> dsKeys) {
        log.debug("选择数据源策略 - 可用数据源: {}", dsKeys);
        
        // 只有一个数据源，直接返回
        if (dsKeys.size() == 1) {
            log.debug("只有一个数据源，直接使用: {}", dsKeys.get(0));
            return dsKeys.get(0);
        }

        // 获取SQL类型和事务状态
        SqlType sqlType = SQL_TYPE_HOLDER.get();
        Boolean inTransaction = TRANSACTION_HOLDER.get();
        
        // 清理ThreadLocal避免内存泄漏
        SQL_TYPE_HOLDER.remove();
        
        // 默认走主库保证数据一致性
        if (sqlType == null) {
            sqlType = SqlType.WRITE;
        }
        
        // 决策逻辑：写操作或事务中强制主库
        if (sqlType == SqlType.WRITE || Boolean.TRUE.equals(inTransaction)) {
            log.debug("写操作或事务中，路由到主库 - SQL类型: {}, 事务状态: {}", sqlType, inTransaction);
            return getMasterDataSource(dsKeys);
        } else {
            log.debug("读操作，路由到从库 - K8s自动负载均衡");
            return getSlaveDataSource(dsKeys);
        }
    }

    /**
     * 获取主库数据源
     */
    private String getMasterDataSource(List<String> dsKeys) {
        // 查找包含master关键字的数据源
        return dsKeys.stream()
                .filter(key -> key.toLowerCase().contains("master"))
                .findFirst()
                .orElse(dsKeys.get(0)); // 如果没找到，返回第一个作为默认
    }

    /**
     * 获取从库数据源
     */
    private String getSlaveDataSource(List<String> dsKeys) {
        // 查找包含slave关键字的数据源
        List<String> slaveKeys = dsKeys.stream()
                .filter(key -> key.toLowerCase().contains("slave"))
                .toList();
        
        if (slaveKeys.isEmpty()) {
            // 没有从库，退回到主库
            log.debug("未找到从库数据源，退回到主库");
            return getMasterDataSource(dsKeys);
        }
        
        // 如果有多个从库，随机选择（K8s Service会处理负载均衡，这里只是演示）
        return slaveKeys.get(ThreadLocalRandom.current().nextInt(slaveKeys.size()));
    }

    /**
     * 设置SQL类型（由拦截器调用）
     */
    public static void setSqlType(SqlType sqlType) {
        SQL_TYPE_HOLDER.set(sqlType);
    }

    /**
     * 获取当前线程的SQL类型
     */
    public static SqlType getSqlType() {
        return SQL_TYPE_HOLDER.get();
    }

    /**
     * 设置事务状态（由事务切面调用）
     */
    public static void setInTransaction(boolean inTransaction) {
        TRANSACTION_HOLDER.set(inTransaction);
    }

    /**
     * 清理事务状态
     */
    public static void clearTransaction() {
        TRANSACTION_HOLDER.remove();
    }

    /**
     * SQL操作类型枚举
     */
    public enum SqlType {
        READ,   // 读操作，路由到从库
        WRITE   // 写操作，路由到主库
    }
}
