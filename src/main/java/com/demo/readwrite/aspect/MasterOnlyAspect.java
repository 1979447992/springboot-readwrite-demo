package com.demo.readwrite.aspect;

import com.demo.readwrite.annotation.MasterOnly;
import com.demo.readwrite.strategy.SimpleReadWriteStrategy;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 强制读主库切面
 * 拦截@MasterOnly注解，强制设置使用主库
 */
@Aspect
@Component
@Order(-1)  // 确保在其他数据源切面之前执行
@Slf4j
public class MasterOnlyAspect {

    @Pointcut("@annotation(com.demo.readwrite.annotation.MasterOnly) || @within(com.demo.readwrite.annotation.MasterOnly)")
    public void masterOnlyPointcut() {}

    /**
     * 方法级别的@MasterOnly注解处理
     */
    @Around("masterOnlyPointcut() && @annotation(masterOnly)")
    public Object aroundMethod(ProceedingJoinPoint point, MasterOnly masterOnly) throws Throwable {
        return executeWithMaster(point, masterOnly, "方法");
    }

    /**
     * 类级别的@MasterOnly注解处理
     */
    @Around("masterOnlyPointcut() && !@annotation(com.demo.readwrite.annotation.MasterOnly) && @within(masterOnly)")
    public Object aroundClass(ProceedingJoinPoint point, MasterOnly masterOnly) throws Throwable {
        return executeWithMaster(point, masterOnly, "类");
    }

    /**
     * 执行强制主库逻辑
     */
    private Object executeWithMaster(ProceedingJoinPoint point, MasterOnly masterOnly, String level) throws Throwable {
        String methodName = point.getSignature().toShortString();
        
        if (masterOnly.logEnabled()) {
            log.info("@MasterOnly拦截({}级别) - 强制使用主库: {} - 方法: {} - 原因: {}", 
                    level, masterOnly.value(), methodName, masterOnly.value());
        }
        
        try {
            // 强制设置为写类型，确保路由到主库
            SimpleReadWriteStrategy.setSqlType(SimpleReadWriteStrategy.SqlType.WRITE);
            return point.proceed();
        } finally {
            if (masterOnly.logEnabled()) {
                log.debug("@MasterOnly执行完成 - 方法: {}", methodName);
            }
        }
    }
}
