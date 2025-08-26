package com.demo.readwrite.controller;

import com.demo.readwrite.service.UserService;
import com.demo.readwrite.service.ConfigService; 
import com.demo.readwrite.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * 混合数据源演示控制器
 * 
 * 功能：
 * 1. 默认业务：自动读写分离 (master/slave)
 * 2. @DS("config")：配置库，单库操作
 * 3. @DS("log")：日志库，单库操作
 */
@RestController
@RequestMapping("/mixed")
public class MixedDataSourceController {

    @Autowired
    private UserService userService;
    
    @Autowired  
    private ConfigService configService;
    
    @Autowired
    private LogService logService;

    /**
     * 测试主业务库读写分离
     */
    @GetMapping("/users")
    public Map<String, Object> getUsers() {
        System.out.println("\n========== 主业务库查询 (应该走SLAVE) ==========");
        
        List<Map<String, Object>> users = userService.findAllUsers();
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "主业务库查询");
        result.put("expected_datasource", "SLAVE (localhost:5433)");
        result.put("users", users);
        result.put("note", "自动读写分离");
        
        System.out.println("========== 主业务库查询结束 ==========\n");
        return result;
    }
    
    @PostMapping("/users")
    public Map<String, Object> createUser(@RequestParam String username, @RequestParam String email) {
        System.out.println("\n========== 主业务库写入 (应该走MASTER) ==========");
        
        Map<String, Object> user = userService.createUser(username, email);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "主业务库写入");
        result.put("expected_datasource", "MASTER (localhost:5432)");
        result.put("user", user);
        result.put("note", "自动读写分离，数据会同步到从库");
        
        System.out.println("========== 主业务库写入结束 ==========\n");
        return result;
    }
    
    /**
     * 测试@DS配置库 - 查询所有配置
     */
    @GetMapping("/config")
    public Map<String, Object> getAllConfigs() {
        System.out.println("\n========== 配置库查询 @DS(\"config\") ==========");
        
        List<Map<String, Object>> configs = configService.getAllConfigs();
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "配置库查询所有配置");
        result.put("datasource", "@DS(\"config\") - localhost:5434");
        result.put("configs", configs);
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
        
        Map<String, Object> config = configService.getConfigByKey(key);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "配置库查询单个配置");
        result.put("key", key);
        result.put("config", config);
        result.put("datasource", "@DS(\"config\") - localhost:5434");
        
        System.out.println("========== 配置库操作结束 ==========\n");
        return result;
    }
    
    /**
     * 根据配置组查询配置
     */
    @GetMapping("/config/group/{group}")
    public Map<String, Object> getConfigsByGroup(@PathVariable String group) {
        System.out.println("\n========== 配置库按组查询 @DS(\"config\") ==========");
        
        List<Map<String, Object>> configs = configService.getConfigsByGroup(group);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "配置库按组查询");
        result.put("group", group);
        result.put("configs", configs);
        result.put("datasource", "@DS(\"config\") - localhost:5434");
        
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
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String group) {
        System.out.println("\n========== 配置库创建 @DS(\"config\") ==========");
        
        Map<String, Object> result = configService.createConfig(key, value, description, group);
        result.put("note", "配置库单库操作，不使用读写分离");
        
        System.out.println("========== 配置库操作结束 ==========\n");
        return result;
    }
    
    /**
     * 更新配置
     */
    @PutMapping("/config/{key}")
    public Map<String, Object> updateConfig(@PathVariable String key, @RequestParam String value) {
        System.out.println("\n========== 配置库更新 @DS(\"config\") ==========");
        
        Map<String, Object> result = configService.updateConfig(key, value);
        result.put("note", "配置库单库操作，不使用读写分离");
        
        System.out.println("========== 配置库操作结束 ==========\n");
        return result;
    }
    
    /**
     * 删除配置
     */
    @DeleteMapping("/config/{key}")
    public Map<String, Object> deleteConfig(@PathVariable String key) {
        System.out.println("\n========== 配置库删除 @DS(\"config\") ==========");
        
        Map<String, Object> result = configService.deleteConfig(key);
        result.put("note", "配置库单库操作，软删除");
        
        System.out.println("========== 配置库操作结束 ==========\n");
        return result;
    }
    
    /**
     * 获取配置分类
     */
    @GetMapping("/config/categories")
    public Map<String, Object> getConfigCategories() {
        System.out.println("\n========== 配置库查询分类 @DS(\"config\") ==========");
        
        List<Map<String, Object>> categories = configService.getConfigCategories();
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "配置库查询分类");
        result.put("categories", categories);
        result.put("datasource", "@DS(\"config\") - localhost:5434");
        
        System.out.println("========== 配置库操作结束 ==========\n");
        return result;
    }
    
    /**
     * 写入应用日志
     */
    @PostMapping("/log/app")
    public Map<String, Object> writeApplicationLog(
            @RequestParam String level,
            @RequestParam String logger, 
            @RequestParam String message,
            @RequestParam(required = false) String thread,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String sessionId) {
        System.out.println("\n========== 日志库写入应用日志 @DS(\"log\") ==========");
        
        Map<String, Object> result = logService.writeApplicationLog(level, logger, message, thread, userId, sessionId);
        result.put("note", "日志库单库操作，不使用读写分离");
        
        System.out.println("========== 日志库操作结束 ==========\n");
        return result;
    }
    
    /**
     * 写入审计日志
     */
    @PostMapping("/log/audit")
    public Map<String, Object> writeAuditLog(
            @RequestParam Long userId,
            @RequestParam String username,
            @RequestParam String action,
            @RequestParam String resource,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String oldValue,
            @RequestParam(required = false) String newValue,
            @RequestParam(required = false) String ipAddress) {
        System.out.println("\n========== 日志库写入审计日志 @DS(\"log\") ==========");
        
        Map<String, Object> result = logService.writeAuditLog(userId, username, action, resource, resourceId, oldValue, newValue, ipAddress);
        result.put("note", "日志库单库操作，不使用读写分离");
        
        System.out.println("========== 日志库操作结束 ==========\n");
        return result;
    }
    
    /**
     * 查询应用日志
     */
    @GetMapping("/log/app")
    public Map<String, Object> getApplicationLogs(
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "10") int limit) {
        System.out.println("\n========== 日志库查询应用日志 @DS(\"log\") ==========");
        
        List<Map<String, Object>> logs = logService.getApplicationLogs(level, limit);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "日志库查询应用日志");
        result.put("logs", logs);
        result.put("datasource", "@DS(\"log\") - localhost:5435");
        
        System.out.println("========== 日志库操作结束 ==========\n");
        return result;
    }
    
    /**
     * 查询审计日志
     */
    @GetMapping("/log/audit")
    public Map<String, Object> getAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "10") int limit) {
        System.out.println("\n========== 日志库查询审计日志 @DS(\"log\") ==========");
        
        List<Map<String, Object>> logs = logService.getAuditLogs(userId, action, limit);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "日志库查询审计日志");
        result.put("logs", logs);
        result.put("datasource", "@DS(\"log\") - localhost:5435");
        
        System.out.println("========== 日志库操作结束 ==========\n");
        return result;
    }
    
    /**
     * 记录系统指标
     */
    @PostMapping("/log/metric")
    public Map<String, Object> recordMetric(
            @RequestParam String name,
            @RequestParam Double value,
            @RequestParam(required = false) String unit,
            @RequestParam(required = false) String tags) {
        System.out.println("\n========== 日志库记录指标 @DS(\"log\") ==========");
        
        Map<String, Object> tagMap = null;
        if (tags != null) {
            tagMap = Map.of("raw", tags);
        }
        
        Map<String, Object> result = logService.recordSystemMetric(name, value, unit, tagMap);
        result.put("note", "日志库单库操作，记录系统指标");
        
        System.out.println("========== 日志库操作结束 ==========\n");
        return result;
    }
    
    /**
     * 查询系统指标
     */
    @GetMapping("/log/metric/{name}")
    public Map<String, Object> getMetrics(
            @PathVariable String name,
            @RequestParam(defaultValue = "24") int hours) {
        System.out.println("\n========== 日志库查询指标 @DS(\"log\") ==========");
        
        List<Map<String, Object>> metrics = logService.getSystemMetrics(name, hours);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "日志库查询指标");
        result.put("metric_name", name);
        result.put("metrics", metrics);
        result.put("datasource", "@DS(\"log\") - localhost:5435");
        
        System.out.println("========== 日志库操作结束 ==========\n");
        return result;
    }
    
    /**
     * 测试数据同步验证
     */
    @GetMapping("/sync-test") 
    public Map<String, Object> testSync() {
        Map<String, Object> result = new HashMap<>();
        
        // 测试主从同步
        int masterCount = userService.countUsersFromMaster();
        int slaveCount = userService.countUsersFromSlave(); 
        
        result.put("master_count", masterCount);
        result.put("slave_count", slaveCount);
        result.put("sync_status", masterCount == slaveCount ? "✅ 同步正常" : "⚠️ 同步延迟");
        result.put("note", "主库写入，从库同步验证");
        
        return result;
    }
    
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("architecture", "多数据源 + 读写分离混合");
        result.put("datasources", Map.of(
            "主业务库", "master(5432) + slave(5433) - 读写分离",
            "配置库", "config(5434) - @DS单库",
            "日志库", "log(5435) - @DS单库"
        ));
        result.put("test_apis", Arrays.asList(
            "GET /mixed/users - 主库读写分离查询",
            "POST /mixed/users - 主库读写分离写入",
            "GET /mixed/config - @DS配置库查询所有配置", 
            "GET /mixed/config/{key} - @DS配置库查询单个配置",
            "GET /mixed/config/group/{group} - @DS配置库按组查询",
            "POST /mixed/config - @DS配置库创建配置",
            "PUT /mixed/config/{key} - @DS配置库更新配置",
            "DELETE /mixed/config/{key} - @DS配置库删除配置",
            "GET /mixed/config/categories - @DS配置库查询分类",
            "POST /mixed/log/app - @DS日志库写入应用日志",
            "POST /mixed/log/audit - @DS日志库写入审计日志",
            "GET /mixed/log/app - @DS日志库查询应用日志",
            "GET /mixed/log/audit - @DS日志库查询审计日志",
            "POST /mixed/log/metric - @DS日志库记录系统指标",
            "GET /mixed/log/metric/{name} - @DS日志库查询指标",
            "GET /mixed/sync-test - 主从同步测试"
        ));
        return result;
    }
}