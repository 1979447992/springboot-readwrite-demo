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

import java.lang.reflect.Method;

@Aspect
@Component
@Order(1)
public class DataSourceAspect {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceAspect.class);

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

            // 检查方法或类上是否有@MasterDB注解
            boolean useMaster = method.isAnnotationPresent(MasterDB.class) 
                             || targetClass.isAnnotationPresent(MasterDB.class);

            // 如果没有@MasterDB注解，根据方法名判断
            if (!useMaster) {
                String methodName = method.getName();
                useMaster = isWriteOperation(methodName);
            }

            if (useMaster) {
                DataSourceContextHolder.setMaster();
                logger.debug("使用主库执行方法: {}.{}", targetClass.getSimpleName(), method.getName());
            } else {
                DataSourceContextHolder.setSlave();
                logger.debug("使用从库执行方法: {}.{}", targetClass.getSimpleName(), method.getName());
            }

            return point.proceed();
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
    }

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