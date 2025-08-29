package com.demo.readwrite.routing;

import com.demo.readwrite.enums.DataSourceType;

public class DataSourceContextHolder {
    
    private static final ThreadLocal<DataSourceType> contextHolder = new ThreadLocal<>();
    
    public static void setDataSource(DataSourceType dataSourceType) {
        contextHolder.set(dataSourceType);
    }
    
    public static DataSourceType getDataSource() {
        return contextHolder.get();
    }
    
    public static void clearDataSource() {
        contextHolder.remove();
    }
    
    public static void setMaster() {
        setDataSource(DataSourceType.MASTER);
    }
    
    public static void setSlave() {
        setDataSource(DataSourceType.SLAVE);
    }
    
    public static boolean isMaster() {
        return DataSourceType.MASTER.equals(getDataSource());
    }
    
    public static boolean isSlave() {
        return DataSourceType.SLAVE.equals(getDataSource());
    }
}