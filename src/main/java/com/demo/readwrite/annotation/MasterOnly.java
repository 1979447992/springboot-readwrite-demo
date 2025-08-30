package com.demo.readwrite.annotation;

import java.lang.annotation.*;

/**
 * 强制读主库注解
 * 标记的方法将强制使用主库，忽略读写分离策略
 * 适用于对数据一致性要求极高的查询场景
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MasterOnly {
    
    /**
     * 注解说明
     */
    String value() default "强制使用主库";
    
    /**
     * 是否记录日志
     */
    boolean logEnabled() default true;
}
