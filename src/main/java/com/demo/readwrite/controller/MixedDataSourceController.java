package com.demo.readwrite.controller;

import com.demo.readwrite.entity.SystemConfig;
import com.demo.readwrite.entity.User;
import com.demo.readwrite.service.UserService;
import com.demo.readwrite.service.ConfigService; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * 混合数据源演示控制器
 * 
 * 功能：
 * 1. 默认业务：自动读写分离 (master/slave) - ShardingSphere管理
 * 2. @DS("config")：配置库，单库操作 - Baomidou动态数据源管理
 */
@RestController
@RequestMapping("/mixed")
public class MixedDataSourceController {

    @Autowired
    private UserService userService;
    
    @Autowired  
    private ConfigService configService;

    /**
     * 测试主业务库读写分离 - 查询用户
     */
    @GetMapping("/users")
    public Map<String, Object> getUsers() {
        System.out.println("\n========== 主业务库查询 (应该走SLAVE) ==========");
        
        List<User> users = userService.findAllUsers();
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "主业务库查询");
        result.put("expected_datasource", "SLAVE (localhost:3307)");
        result.put("users", users);
        result.put("count", users.size());
        result.put("note", "ShardingSphere自动读写分离");
        
        System.out.println("========== 主业务库查询结束 ==========\n");
        return result;
    }
    
    /**
     * 测试主业务库读写分离 - 创建用户
     */
    @PostMapping("/users")
    public Map<String, Object> createUser(
            @RequestParam String username, 
            @RequestParam String email,
            @RequestParam(defaultValue = "25") Integer age) {
        System.out.println("\n========== 主业务库写入 (应该走MASTER) ==========");
        
        User user = userService.createUser(username, email, age);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "主业务库写入");
        result.put("expected_datasource", "MASTER (localhost:3306)");
        result.put("user", user);
        result.put("note", "ShardingSphere自动路由到主库，数据会同步到从库");
        
        System.out.println("========== 主业务库写入结束 ==========\n");
        return result;
    }
    
    /**
     * 测试@DS配置库 - 查询所有配置
     */
    @GetMapping("/config")
    public Map<String, Object> getAllConfigs() {
        System.out.println("\n========== 配置库查询 @DS(\"config\") ==========");
        
        List<SystemConfig> configs = configService.getAllConfigs();
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "配置库查询所有配置");
        result.put("datasource", "@DS(\"config\") - localhost:3308");
        result.put("configs", configs);
        result.put("count", configs.size());
        result.put("note", "不进行读写分离，直接使用指定库");
        
        System.out.println("========== 配置库操作结束 ==========\n");
        return result;
    }
    
    /**
     * 根据配置键查询配置
     */
    @GetMapping("/config/{key}")
    public Map<String, Object> getConfigByKey(@PathVariable String key) {
        System.out.println("\n========== 配置库查询单个配置 @DS(\"config\") ==========");
        
        SystemConfig config = configService.getConfigByKey(key);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "配置库查询单个配置");
        result.put("key", key);
        result.put("config", config);
        result.put("found", config != null);
        result.put("datasource", "@DS(\"config\") - localhost:3308");
        
        System.out.println("========== 配置库操作结束 ==========\n");
        return result;
    }
    
    /**
     * 根据ID查询配置
     */
    @GetMapping("/config/id/{id}")
    public Map<String, Object> getConfigById(@PathVariable Long id) {
        System.out.println("\n========== 配置库根据ID查询 @DS(\"config\") ==========");
        
        SystemConfig config = configService.getConfigById(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "配置库根据ID查询");
        result.put("id", id);
        result.put("config", config);
        result.put("found", config != null);
        result.put("datasource", "@DS(\"config\") - localhost:3308");
        
        System.out.println("========== 配置库操作结束 ==========\n");
        return result;
    }
    
    /**
     * 创建新配置
     */
    @PostMapping("/config")
    public Map<String, Object> createConfig(
            @RequestParam String key, 
            @RequestParam String value,
            @RequestParam(required = false) String description) {
        System.out.println("\n========== 配置库创建 @DS(\"config\") ==========");
        
        Map<String, Object> result = configService.createConfig(key, value, description != null ? description : "");
        result.put("note", "配置库单库操作，不使用读写分离");
        
        System.out.println("========== 配置库操作结束 ==========\n");
        return result;
    }
    
    /**
     * 更新配置
     */
    @PutMapping("/config/{key}")
    public Map<String, Object> updateConfig(
            @PathVariable String key, 
            @RequestParam String value,
            @RequestParam(required = false) String description) {
        System.out.println("\n========== 配置库更新 @DS(\"config\") ==========");
        
        Map<String, Object> result = configService.updateConfig(key, value, description != null ? description : "");
        result.put("note", "配置库单库操作，不使用读写分离");
        
        System.out.println("========== 配置库操作结束 ==========\n");
        return result;
    }
    
    /**
     * 删除配置
     */
    @DeleteMapping("/config/{id}")
    public Map<String, Object> deleteConfig(@PathVariable Long id) {
        System.out.println("\n========== 配置库删除 @DS(\"config\") ==========");
        
        Map<String, Object> result = configService.deleteConfig(id);
        result.put("note", "配置库单库操作，物理删除");
        
        System.out.println("========== 配置库操作结束 ==========\n");
        return result;
    }
    
    /**
     * 测试数据同步验证
     */
    @GetMapping("/sync-test") 
    public Map<String, Object> testSync() {
        Map<String, Object> result = new HashMap<>();
        
        // 统计用户数量
        int userCount = userService.countUsers();
        
        result.put("user_count", userCount);
        result.put("note", "主库写入，从库读取，验证主从同步");
        result.put("suggestion", "先通过POST /mixed/users创建一些用户，然后查询验证同步");
        
        return result;
    }
    
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("architecture", "多数据源 + 读写分离混合 - MySQL版本");
        result.put("datasources", Map.of(
            "主业务库", "master(3306) + slave(3307) - ShardingSphere读写分离",
            "配置库", "config(3308) - @DS单库"
        ));
        result.put("test_apis", Arrays.asList(
            "GET /mixed/users - 主库读写分离查询",
            "POST /mixed/users - 主库读写分离写入",
            "GET /mixed/config - @DS配置库查询所有配置", 
            "GET /mixed/config/{key} - @DS配置库按键查询",
            "GET /mixed/config/id/{id} - @DS配置库按ID查询",
            "POST /mixed/config - @DS配置库创建配置",
            "PUT /mixed/config/{key} - @DS配置库更新配置",
            "DELETE /mixed/config/{id} - @DS配置库删除配置",
            "GET /mixed/sync-test - 主从同步测试"
        ));
        result.put("version", "2.0-MySQL");
        result.put("timestamp", new Date().toString());
        return result;
    }
}