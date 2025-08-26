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
     * 测试@DS配置库 - 单库操作
     */
    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        System.out.println("\n========== 配置库操作 @DS(\"config\") ==========");
        
        Map<String, Object> config = configService.getAppConfig();
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "配置库查询");
        result.put("datasource", "@DS(\"config\") - localhost:5434");
        result.put("config", config);
        result.put("note", "不进行读写分离，直接使用指定库");
        
        System.out.println("========== 配置库操作结束 ==========\n");
        return result;
    }
    
    /**
     * 测试@DS日志库 - 单库操作  
     */
    @PostMapping("/log")
    public Map<String, Object> writeLog(@RequestParam String message) {
        System.out.println("\n========== 日志库操作 @DS(\"log\") ==========");
        
        Map<String, Object> log = logService.writeLog(message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "日志库写入");
        result.put("datasource", "@DS(\"log\") - localhost:5435");
        result.put("log", log);
        result.put("note", "不进行读写分离，直接使用指定库");
        
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
            "GET /mixed/config - @DS配置库查询", 
            "POST /mixed/log - @DS日志库写入",
            "GET /mixed/sync-test - 主从同步测试"
        ));
        return result;
    }
}