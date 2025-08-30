package com.demo.readwrite.aspect;

import com.demo.readwrite.strategy.SimpleReadWriteStrategy;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 事务切面
 * 标记事务状态，确保事务中的所有操作都使用主库
 */
@Aspect
@Component
@Order(-2) // 确保在@MasterOnly切面之前执行
@Slf4j
public class TransactionAspect {

    /**
     * 拦截@Transactional注解的方法
     */
    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object aroundTransactional(ProceedingJoinPoint point) throws Throwable {
        String methodName = point.getSignature().toShortString();
        
        try {
            log.debug("事务开始 - 设置强制使用主库: {}", methodName);
            SimpleReadWriteStrategy.setInTransaction(true);
            return point.proceed();
        } finally {
            SimpleReadWriteStrategy.clearTransaction();
            log.debug("事务结束 - 清除事务标记: {}", methodName);
        }
    }

    /**
     * 拦截类级别@Transactional注解
     */
    @Around("@within(org.springframework.transaction.annotation.Transactional) && " +
            "!@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object aroundClassTransactional(ProceedingJoinPoint point) throws Throwable {
        String methodName = point.getSignature().toShortString();
        
        try {
            log.debug("类级别事务开始 - 设置强制使用主库: {}", methodName);
            SimpleReadWriteStrategy.setInTransaction(true);
            return point.proceed();
        } finally {
            SimpleReadWriteStrategy.clearTransaction();
            log.debug("类级别事务结束 - 清除事务标记: {}", methodName);
        }
    }
}
