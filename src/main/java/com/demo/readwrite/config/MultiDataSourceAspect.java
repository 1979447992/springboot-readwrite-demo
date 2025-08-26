package com.demo.readwrite.config;

import com.demo.readwrite.DataSourceType;
import com.demo.readwrite.DataSourceContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Method;

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
@Order(1)
public class MultiDataSourceAspect {

    private static final Logger logger = LoggerFactory.getLogger(MultiDataSourceAspect.class);
    
    @Around("execution(* com.demo.readwrite.service.*.*(..))")
    public Object routeDataSource(ProceedingJoinPoint point) throws Throwable {
        String methodName = point.getSignature().getName();
        String className = point.getTarget().getClass().getSimpleName();
        
        boolean hasCustomDS = hasCustomDataSourceAnnotation(point);
        
        if (hasCustomDS) {
            logger.debug("🎯 [CUSTOM-DS] {}.{} - 使用@DS指定数据源，跳过读写分离", className, methodName);
            return executeWithErrorHandling(point, className, methodName);
        }
        
        try {
            if (isWriteOperation(methodName)) {
                DataSourceContextHolder.setMaster();
                logger.debug("🔴 [MASTER] {}.{} - 写操作，使用主库", className, methodName);
            } else {
                DataSourceContextHolder.setSlave();
                logger.debug("🔵 [SLAVE] {}.{} - 读操作，使用从库", className, methodName);
            }
            
            return executeWithErrorHandling(point, className, methodName);
            
        } finally {
            if (!hasCustomDS) {
                DataSourceContextHolder.clearDataSource();
                logger.debug("🧹 清除读写分离上下文 - {}", Thread.currentThread().getName());
            }
        }
    }
    
    private Object executeWithErrorHandling(ProceedingJoinPoint point, String className, String methodName) throws Throwable {
        try {
            return point.proceed();
        } catch (Exception e) {
            logger.error("数据库操作失败: {}.{} - {}", className, methodName, e.getMessage());
            
            // 如果是读操作失败且当前是从库，可以尝试主库
            if (!isWriteOperation(methodName) && DataSourceContextHolder.isSlave()) {
                logger.warn("从库操作失败，尝试切换到主库重试: {}.{}", className, methodName);
                try {
                    DataSourceContextHolder.setMaster();
                    return point.proceed();
                } catch (Exception retryException) {
                    logger.error("主库重试也失败: {}.{} - {}", className, methodName, retryException.getMessage());
                    throw retryException;
                } finally {
                    DataSourceContextHolder.clearDataSource();
                }
            }
            
            throw e;
        }
    }
    
    /**
     * 检查是否使用@DS注解指定了自定义数据源
     */
    private boolean hasCustomDataSourceAnnotation(ProceedingJoinPoint point) {
        try {
            Class<?> targetClass = point.getTarget().getClass();
            String methodName = point.getSignature().getName();
            String className = targetClass.getSimpleName();
            
            // 检查类级别的@DS注解
            if (targetClass.isAnnotationPresent(com.baomidou.dynamic.datasource.annotation.DS.class)) {
                return true;
            }
            
            // 检查方法级别的@DS注解
            try {
                Method method = targetClass.getMethod(methodName, getParameterTypes(point));
                if (method.isAnnotationPresent(com.baomidou.dynamic.datasource.annotation.DS.class)) {
                    return true;
                }
            } catch (NoSuchMethodException ignored) {
                // 方法不存在时忽略
            }
            
            // 备用检查：基于命名约定
            return methodName.contains("Config") || methodName.contains("Log") 
                || className.contains("Config") || className.contains("Log");
        } catch (Exception e) {
            System.err.println("检查@DS注解时发生错误: " + e.getMessage());
            return false;
        }
    }
    
    private Class<?>[] getParameterTypes(ProceedingJoinPoint point) {
        Object[] args = point.getArgs();
        Class<?>[] paramTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
        }
        return paramTypes;
    }
    
    /**
     * 判断是否为写操作
     */
    private boolean isWriteOperation(String methodName) {
        // 写操作前缀匹配
        String[] writePrefixes = {"save", "create", "update", "delete", "insert", "remove", "modify", "add", "write", "record"};
        String lowerMethodName = methodName.toLowerCase();
        
        for (String prefix : writePrefixes) {
            if (lowerMethodName.startsWith(prefix)) {
                return true;
            }
        }
        
        // 特殊写操作关键词
        String[] writeKeywords = {"cleanup", "clear", "reset", "batch", "bulk", "execute"};
        for (String keyword : writeKeywords) {
            if (lowerMethodName.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isReadOperation(String methodName) {
        // 读操作前缀匹配
        String[] readPrefixes = {"find", "get", "query", "select", "search", "list", "count", "exists", "check"};
        String lowerMethodName = methodName.toLowerCase();
        
        for (String prefix : readPrefixes) {
            if (lowerMethodName.startsWith(prefix)) {
                return true;
            }
        }
        
        return false;
    }
}