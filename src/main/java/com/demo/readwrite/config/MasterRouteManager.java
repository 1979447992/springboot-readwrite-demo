package com.demo.readwrite.config;

import org.apache.shardingsphere.readwritesplitting.api.hint.HintManager;
import org.springframework.stereotype.Component;

/**
 * ShardingSphere强制主库路由工具
 * 用于关键业务操作强制使用主库
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
     */
    public <T> T executeOnMaster(MasterOperation<T> operation) {
        HintManager hintManager = HintManager.getInstance();
        try {
            // 强制路由到主库
            hintManager.setWriteRouteOnly();
            return operation.execute();
        } finally {
            hintManager.close();
        }
    }
    
    /**
     * 无返回值的强制主库操作
     */
    public void executeOnMaster(MasterOperationVoid operation) {
        HintManager hintManager = HintManager.getInstance();
        try {
            hintManager.setWriteRouteOnly();
            operation.execute();
        } finally {
            hintManager.close();
        }
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