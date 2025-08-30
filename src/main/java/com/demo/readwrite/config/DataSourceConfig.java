package com.demo.readwrite.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.demo.readwrite.interceptor.ReadWriteSplitInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据源配置类
 */
@Configuration
@Slf4j
public class DataSourceConfig {

    /**
     * MyBatis-Plus拦截器配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        log.info("配置MyBatis-Plus拦截器");
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        
        log.info("MyBatis-Plus拦截器配置完成");
        return interceptor;
    }
    
    /**
     * 注册读写分离拦截器
     */
    @Bean
    public Interceptor readWriteSplitInterceptor() {
        log.info("注册读写分离拦截器");
        return new ReadWriteSplitInterceptor();
    }
}
