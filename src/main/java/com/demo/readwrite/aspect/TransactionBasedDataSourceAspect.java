package com.demo.readwrite.aspect;

import com.demo.readwrite.annotation.MasterDB;
import com.demo.readwrite.routing.DataSourceContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;

/**
 * 基于事务状态的数据源路由切面
 * 这种方案适用于任何项目，无需关心方法命名规范
 */
@Aspect
@Component
@Order(1) // 确保在事务切面之前执行
public class TransactionBasedDataSourceAspect {

    private static final Logger logger = LoggerFactory.getLogger(TransactionBasedDataSourceAspect.class);

    @Pointcut("@annotation(com.demo.readwrite.annotation.MasterDB) || @within(com.demo.readwrite.annotation.MasterDB)")
    public void masterDBPointcut() {}

    @Pointcut("execution(* com.demo.readwrite.service.*.*(..))")
    public void servicePointcut() {}

    @Around("servicePointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            Class<?> targetClass = point.getTarget().getClass();

            // 1. 首先检查是否有@MasterDB注解（最高优先级）
            boolean forceMaster = method.isAnnotationPresent(MasterDB.class) 
                               || targetClass.isAnnotationPresent(MasterDB.class);

            if (forceMaster) {
                DataSourceContextHolder.setMaster();
                logger.debug("@MasterDB注解强制使用主库: {}.{}", targetClass.getSimpleName(), method.getName());
            } else {
                // 2. 基于事务状态判断
                if (isInWriteTransaction()) {
                    DataSourceContextHolder.setMaster();
                    logger.debug("检测到写事务，使用主库: {}.{}", targetClass.getSimpleName(), method.getName());
                } else {
                    // 3. MyBatis Plus方法名判断（作为补充）
                    String methodName = method.getName();
                    if (isWriteOperation(methodName)) {
                        DataSourceContextHolder.setMaster();
                        logger.debug("写操作方法，使用主库: {}.{}", targetClass.getSimpleName(), method.getName());
                    } else {
                        DataSourceContextHolder.setSlave();
                        logger.debug("读操作方法，使用从库: {}.{}", targetClass.getSimpleName(), method.getName());
                    }
                }
            }

            return point.proceed();
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
    }

    /**
     * 判断当前是否在写事务中
     * 这是最可靠的判断方式，因为写操作通常都会开启事务
     */
    private boolean isInWriteTransaction() {
        // 检查是否在事务中
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            return false;
        }
        
        // 检查事务是否为只读
        // 注意：只有明确标记为readOnly=true的事务才被认为是只读事务
        return !TransactionSynchronizationManager.isCurrentTransactionReadOnly();
    }

    /**
     * MyBatis Plus常用方法判断（作为辅助）
     * 适用于没有事务的简单操作
     */
    private boolean isWriteOperation(String methodName) {
        // MyBatis Plus Service层写操作方法
        if (methodName.startsWith("save") || methodName.startsWith("insert") 
            || methodName.startsWith("update") || methodName.startsWith("delete") 
            || methodName.startsWith("remove") || methodName.startsWith("create") 
            || methodName.startsWith("modify") || methodName.startsWith("add") 
            || methodName.startsWith("edit")) {
            return true;
        }
        
        // MyBatis Plus自带的写操作方法
        return methodName.equals("saveOrUpdate") 
            || methodName.equals("saveOrUpdateBatch")
            || methodName.equals("updateById") 
            || methodName.equals("updateBatchById")
            || methodName.equals("removeById") 
            || methodName.equals("removeByIds")
            || methodName.equals("removeByMap") 
            || methodName.equals("remove")  // remove(Wrapper)
            || methodName.equals("saveBatch")
            || methodName.equals("updateBatch");
    }
}