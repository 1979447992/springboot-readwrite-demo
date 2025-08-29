package com.demo.readwrite.config;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ShardingSphere强制主库路由工具
 * 用于关键业务操作强制使用主库
 * ShardingSphere 5.x版本推荐使用事务机制来控制读写路由
 */
@Component
public class MasterRouteManager {
    
    /**
     * 强制使用主库执行操作
     * 使用方式：
     * masterRouteManager.executeOnMaster(() -> {
     *     // 这里的所有数据库操作都会路由到主库
     *     return userService.getUserById(id);
     * });
     * 
     * 注意：在事务中的所有操作都会路由到主库
     */
    @Transactional
    public <T> T executeOnMaster(MasterOperation<T> operation) {
        // 在事务中执行，自动路由到主库
        return operation.execute();
    }
    
    /**
     * 无返回值的强制主库操作
     */
    @Transactional
    public void executeOnMaster(MasterOperationVoid operation) {
        // 在事务中执行，自动路由到主库
        operation.execute();
    }
    
    @FunctionalInterface
    public interface MasterOperation<T> {
        T execute();
    }
    
    @FunctionalInterface
    public interface MasterOperationVoid {
        void execute();
    }
}