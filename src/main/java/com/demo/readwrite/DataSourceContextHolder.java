package com.demo.readwrite;

public class DataSourceContextHolder {
    
    private static final ThreadLocal<DataSourceType> contextHolder = new ThreadLocal<>();
    
    public static void setMaster() {
        contextHolder.set(DataSourceType.MASTER);
        System.out.println("🔴 [MASTER] 切换到主库 - " + Thread.currentThread().getName());
    }
    
    public static void setSlave() {
        contextHolder.set(DataSourceType.SLAVE);
        System.out.println("🔵 [SLAVE] 切换到从库 - " + Thread.currentThread().getName());
    }
    
    public static DataSourceType getDataSource() {
        return contextHolder.get();
    }
    
    public static void clearDataSource() {
        contextHolder.remove();
        System.out.println("🧹 清除数据源上下文 - " + Thread.currentThread().getName());
    }
    
    public static boolean isMaster() {
        return DataSourceType.MASTER.equals(getDataSource());
    }
    
    public static boolean isSlave() {
        return DataSourceType.SLAVE.equals(getDataSource());
    }
}