package com.demo.readwrite.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import java.sql.ResultSet;
import java.sql.SQLException;
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
     * 获取所有配置 - 使用@DS指定配置库
     */
    @DS("config")
    public List<Map<String, Object>> getAllConfigs() {
        System.out.println("🔧 [CONFIG-DB] 查询所有配置 - 使用 @DS(\"config\") - localhost:5434");
        
        String sql = "SELECT * FROM config_settings WHERE is_active = true ORDER BY config_group, config_key";
        return jdbcTemplate.queryForList(sql);
    }
    
    /**
     * 根据配置键获取配置值
     */
    @DS("config")
    public Map<String, Object> getConfigByKey(String configKey) {
        System.out.println("🔧 [CONFIG-DB] 查询配置: " + configKey + " - 使用 @DS(\"config\")");
        
        String sql = "SELECT * FROM config_settings WHERE config_key = ? AND is_active = true";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, configKey);
        
        if (!results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }
    
    /**
     * 根据配置组获取配置
     */
    @DS("config")
    public List<Map<String, Object>> getConfigsByGroup(String configGroup) {
        System.out.println("🔧 [CONFIG-DB] 查询配置组: " + configGroup + " - 使用 @DS(\"config\")");
        
        String sql = "SELECT * FROM config_settings WHERE config_group = ? AND is_active = true ORDER BY config_key";
        return jdbcTemplate.queryForList(sql, configGroup);
    }
    
    /**
     * 创建新配置
     */
    @DS("config")
    public Map<String, Object> createConfig(String configKey, String configValue, String description, String configGroup) {
        System.out.println("🔧 [CONFIG-DB] 创建配置: " + configKey + " - 使用 @DS(\"config\")");
        
        String sql = "INSERT INTO config_settings (config_key, config_value, description, config_group) VALUES (?, ?, ?, ?)";
        int rows = jdbcTemplate.update(sql, configKey, configValue, description, configGroup);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "CREATE_CONFIG");
        result.put("key", configKey);
        result.put("value", configValue);
        result.put("affected_rows", rows);
        result.put("datasource", "config库 (localhost:5434)");
        result.put("timestamp", new Date().toString());
        
        return result;
    }
    
    /**
     * 更新配置
     */
    @DS("config") 
    public Map<String, Object> updateConfig(String configKey, String configValue) {
        System.out.println("🔧 [CONFIG-DB] 更新配置: " + configKey + " - 使用 @DS(\"config\")");
        
        String sql = "UPDATE config_settings SET config_value = ?, updated_at = CURRENT_TIMESTAMP WHERE config_key = ?";
        int rows = jdbcTemplate.update(sql, configValue, configKey);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "UPDATE_CONFIG");
        result.put("key", configKey);
        result.put("value", configValue);
        result.put("affected_rows", rows);
        result.put("datasource", "config库 (localhost:5434)");
        result.put("timestamp", new Date().toString());
        
        return result;
    }
    
    /**
     * 删除配置 (软删除)
     */
    @DS("config")
    public Map<String, Object> deleteConfig(String configKey) {
        System.out.println("🔧 [CONFIG-DB] 删除配置: " + configKey + " - 使用 @DS(\"config\")");
        
        String sql = "UPDATE config_settings SET is_active = false, updated_at = CURRENT_TIMESTAMP WHERE config_key = ?";
        int rows = jdbcTemplate.update(sql, configKey);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "DELETE_CONFIG");
        result.put("key", configKey);
        result.put("affected_rows", rows);
        result.put("datasource", "config库 (localhost:5434)");
        result.put("timestamp", new Date().toString());
        
        return result;
    }
    
    /**
     * 获取所有配置分类
     */
    @DS("config")
    public List<Map<String, Object>> getConfigCategories() {
        System.out.println("🔧 [CONFIG-DB] 查询配置分类 - 使用 @DS(\"config\")");
        
        String sql = "SELECT * FROM config_categories ORDER BY category_name";
        return jdbcTemplate.queryForList(sql);
    }
}