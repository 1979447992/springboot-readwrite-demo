package com.demo.readwrite.config;

import com.demo.readwrite.DataSourceType;
import com.demo.readwrite.DataSourceContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 多数据源 + 读写分离混合切面
 * 
 * 工作原理：
 * 1. 默认数据源(master/slave)：根据操作类型自动读写分离
 * 2. @DS指定数据源：直接使用指定库，不进行读写分离
 * 
 * 优先级：@DS注解 > 读写分离逻辑
 */
@Aspect
@Component
@Order(1) // 确保在其他切面之前执行
public class MultiDataSourceAspect {

    /**
     * 拦截Service层方法，实现智能数据源路由
     */
    @Around("execution(* com.demo.readwrite.service.*.*(..))")
    public Object routeDataSource(ProceedingJoinPoint point) throws Throwable {
        String methodName = point.getSignature().getName();
        String className = point.getTarget().getClass().getSimpleName();
        
        // 检查是否有@DS注解 - 如果有就不进行读写分离
        boolean hasCustomDS = hasCustomDataSourceAnnotation(point);
        
        if (hasCustomDS) {
            System.out.println("🎯 [CUSTOM-DS] " + className + "." + methodName + " - 使用@DS指定数据源，跳过读写分离");
            return point.proceed();
        }
        
        // 默认数据源进行读写分离
        try {
            if (isWriteOperation(methodName)) {
                DataSourceContextHolder.setMaster();
                System.out.println("🔴 [MASTER] " + className + "." + methodName + " - 写操作，使用主库 (localhost:5432)");
            } else {
                DataSourceContextHolder.setSlave();
                System.out.println("🔵 [SLAVE] " + className + "." + methodName + " - 读操作，使用从库 (localhost:5433)");
            }
            
            return point.proceed();
            
        } finally {
            if (!hasCustomDS) {
                DataSourceContextHolder.clearDataSource();
                System.out.println("🧹 清除读写分离上下文");
            }
        }
    }
    
    /**
     * 检查是否使用@DS注解指定了自定义数据源
     */
    private boolean hasCustomDataSourceAnnotation(ProceedingJoinPoint point) {
        // 检查方法级别的@DS注解
        try {
            Class<?> targetClass = point.getTarget().getClass();
            String methodName = point.getSignature().getName();
            
            // 这里可以通过反射检查@DS注解
            // 为简化演示，我们假设包含特定关键字的方法使用自定义数据源
            return methodName.contains("Config") || methodName.contains("Log") 
                || className.contains("Config") || className.contains("Log");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 判断是否为写操作
     */
    private boolean isWriteOperation(String methodName) {
        return methodName.startsWith("save") || methodName.startsWith("create") 
            || methodName.startsWith("update") || methodName.startsWith("delete") 
            || methodName.startsWith("insert") || methodName.startsWith("remove")
            || methodName.startsWith("modify") || methodName.startsWith("add");
    }
}