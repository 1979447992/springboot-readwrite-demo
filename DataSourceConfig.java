package com.demo.readwrite.config;

import com.demo.readwrite.DataSourceType;
import com.demo.readwrite.DataSourceContextHolder;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {
    
    @Bean
    @ConfigurationProperties("spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }
    
    @Bean  
    @ConfigurationProperties("spring.datasource.slave")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }
    
    @Bean
    @Primary
    public DataSource routingDataSource() {
        DynamicDataSource routing = new DynamicDataSource();
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(DataSourceType.MASTER.getValue(), masterDataSource());
        dataSourceMap.put(DataSourceType.SLAVE.getValue(), slaveDataSource());
        routing.setTargetDataSources(dataSourceMap);
        routing.setDefaultTargetDataSource(slaveDataSource()); // 默认从库
        return routing;
    }
    
    private static class DynamicDataSource extends AbstractRoutingDataSource {
        @Override
        protected Object determineCurrentLookupKey() {
            DataSourceType dataSource = DataSourceContextHolder.getDataSource();
            if (dataSource == null) {
                System.out.println("🔵 [DEFAULT] 使用默认从库");
                return DataSourceType.SLAVE.getValue();
            }
            System.out.println("🎯 [ROUTING] 路由到: " + dataSource.getValue());
            return dataSource.getValue();
        }
    }
}