package com.demo.readwrite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceContextHolder {
    
    private static final Logger logger = LoggerFactory.getLogger(DataSourceContextHolder.class);
    private static final ThreadLocal<DataSourceType> contextHolder = new ThreadLocal<DataSourceType>() {
        @Override
        protected void finalize() throws Throwable {
            logger.warn("ThreadLocal被GC回收，可能存在内存泄漏风险");
            super.finalize();
        }
    };
    
    public static void setMaster() {
        contextHolder.set(DataSourceType.MASTER);
        logger.debug("🔴 [MASTER] 切换到主库 - {}", Thread.currentThread().getName());
    }
    
    public static void setSlave() {
        contextHolder.set(DataSourceType.SLAVE);
        logger.debug("🔵 [SLAVE] 切换到从库 - {}", Thread.currentThread().getName());
    }
    
    public static DataSourceType getDataSource() {
        return contextHolder.get();
    }
    
    public static void clearDataSource() {
        DataSourceType current = contextHolder.get();
        contextHolder.remove();
        if (current != null) {
            logger.debug("🧹 清除数据源上下文 [{}] - {}", current, Thread.currentThread().getName());
        }
    }
    
    public static boolean isMaster() {
        return DataSourceType.MASTER.equals(getDataSource());
    }
    
    public static boolean isSlave() {
        return DataSourceType.SLAVE.equals(getDataSource());
    }
    
    public static boolean hasDataSource() {
        return contextHolder.get() != null;
    }
    
    public static void forceCleanAll() {
        logger.warn("强制清理所有ThreadLocal上下文");
        contextHolder.remove();
    }
}