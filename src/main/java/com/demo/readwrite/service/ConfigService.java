package com.demo.readwrite.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.demo.readwrite.entity.SystemConfig;
import com.demo.readwrite.mapper.SystemConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * 配置服务 - 使用@DS注解访问独立的config数据库
 * 该服务不参与ShardingSphere读写分离，使用Baomidou动态数据源
 */
@Service
public class ConfigService {

    @Autowired
    private SystemConfigMapper configMapper;
    
    /**
     * 获取所有配置 - 使用@DS注解指定config数据源
     */
    @DS("config")
    public List<SystemConfig> getAllConfigs() {
        System.out.println("🔧 [CONFIG-DB] 查询所有配置 - 使用@DS(\"config\")注解访问独立配置库 localhost:3308");
        return configMapper.selectAll();
    }
    
    /**
     * 根据配置键获取配置值 - 使用@DS注解指定config数据源
     */
    @DS("config")
    public SystemConfig getConfigByKey(String configKey) {
        System.out.println("🔧 [CONFIG-DB] 查询配置: " + configKey + " - 使用@DS(\"config\")注解访问独立配置库");
        return configMapper.selectByConfigKey(configKey);
    }
    
    /**
     * 根据ID获取配置 - 使用@DS注解指定config数据源
     */
    @DS("config")
    public SystemConfig getConfigById(Long id) {
        System.out.println("🔧 [CONFIG-DB] 根据ID查询配置: " + id + " - 使用@DS(\"config\")注解访问独立配置库");
        return configMapper.selectById(id);
    }
    
    /**
     * 创建新配置 - 使用@DS注解指定config数据源
     */
    @DS("config")
    @Transactional
    public Map<String, Object> createConfig(String configKey, String configValue, String description) {
        System.out.println("🔧 [CONFIG-DB] 创建配置: " + configKey + " - 使用@DS(\"config\")注解访问独立配置库");
        
        SystemConfig config = new SystemConfig(configKey, configValue, description);
        int rows = configMapper.insert(config);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "CREATE_CONFIG");
        result.put("key", configKey);
        result.put("value", configValue);
        result.put("id", config.getId());
        result.put("affected_rows", rows);
        result.put("datasource", "@DS(\"config\") - localhost:3308/config_db");
        result.put("timestamp", new Date().toString());
        
        return result;
    }
    
    /**
     * 更新配置 - 使用@DS注解指定config数据源
     */
    @DS("config")
    @Transactional
    public Map<String, Object> updateConfig(String configKey, String configValue, String description) {
        System.out.println("🔧 [CONFIG-DB] 更新配置: " + configKey + " - 使用@DS(\"config\")注解访问独立配置库");
        
        SystemConfig config = new SystemConfig();
        config.setConfigKey(configKey);
        config.setConfigValue(configValue);
        config.setDescription(description);
        
        int rows = configMapper.updateByConfigKey(config);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "UPDATE_CONFIG");
        result.put("key", configKey);
        result.put("value", configValue);
        result.put("affected_rows", rows);
        result.put("datasource", "@DS(\"config\") - localhost:3308/config_db");
        result.put("timestamp", new Date().toString());
        
        return result;
    }
    
    /**
     * 删除配置 - 使用@DS注解指定config数据源
     */
    @DS("config")
    @Transactional
    public Map<String, Object> deleteConfig(Long id) {
        System.out.println("🔧 [CONFIG-DB] 删除配置: " + id + " - 使用@DS(\"config\")注解访问独立配置库");
        
        int rows = configMapper.deleteById(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "DELETE_CONFIG");
        result.put("id", id);
        result.put("affected_rows", rows);
        result.put("datasource", "@DS(\"config\") - localhost:3308/config_db");
        result.put("timestamp", new Date().toString());
        
        return result;
    }
}