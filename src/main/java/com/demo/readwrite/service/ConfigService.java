package com.demo.readwrite.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 配置服务 - 使用@DS("config")指定配置数据库
 * 不进行读写分离，直接使用单一配置库
 */
@Service
public class ConfigService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 获取应用配置 - 使用@DS指定配置库
     * 不会触发读写分离逻辑
     */
    @DS("config")
    public Map<String, Object> getAppConfig() {
        System.out.println("🔧 [CONFIG-DB] 查询配置库 - 使用 @DS(\"config\")");
        
        Map<String, Object> config = new HashMap<>();
        config.put("app_name", "ReadWrite Demo");
        config.put("version", "1.0.0");
        config.put("max_connections", 100);
        config.put("datasource_note", "配置库不分离，单库操作");
        config.put("timestamp", new Date().toString());
        
        // 注意：这里会使用localhost:5434的config数据库
        // 而不是主从分离的5432/5433
        
        return config;
    }
    
    /**
     * 更新配置 - 使用@DS指定配置库
     */
    @DS("config") 
    public Map<String, Object> updateConfig(String key, String value) {
        System.out.println("🔧 [CONFIG-DB] 更新配置库 - 使用 @DS(\"config\")");
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "UPDATE_CONFIG");
        result.put("key", key);
        result.put("value", value);
        result.put("datasource", "config库 (localhost:5434)");
        result.put("note", "不使用读写分离");
        result.put("timestamp", new Date().toString());
        
        return result;
    }
}