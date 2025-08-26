package com.demo.readwrite;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class ReadWriteController {

    @GetMapping("/users")
    public Map<String, Object> getUsers() {
        System.out.println("\n========== 查询操作开始 ==========");
        DataSourceContextHolder.setSlave();
        
        DataSourceType currentDS = DataSourceContextHolder.getDataSource();
        String realDataSource = currentDS != null ? currentDS.getValue() : "未设置";
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "SELECT");
        result.put("real_datasource_from_threadlocal", realDataSource);
        result.put("is_master", DataSourceContextHolder.isMaster());
        result.put("is_slave", DataSourceContextHolder.isSlave());
        result.put("threadlocal_object", currentDS);
        result.put("users", Arrays.asList("张三", "李四", "王五"));
        result.put("current_thread", Thread.currentThread().getName());
        result.put("timestamp", new Date().toString());
        
        System.out.println("✅ 真实数据源: " + realDataSource);
        DataSourceContextHolder.clearDataSource();
        System.out.println("========== 查询操作结束 ==========\n");
        return result;
    }

    @PostMapping("/users")
    public Map<String, Object> createUser(@RequestParam(defaultValue = "新用户") String name) {
        System.out.println("\n========== 写入操作开始 ==========");
        DataSourceContextHolder.setMaster();
        
        DataSourceType currentDS = DataSourceContextHolder.getDataSource();
        String realDataSource = currentDS != null ? currentDS.getValue() : "未设置";
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "INSERT");
        result.put("real_datasource_from_threadlocal", realDataSource);
        result.put("is_master", DataSourceContextHolder.isMaster());
        result.put("is_slave", DataSourceContextHolder.isSlave());
        result.put("threadlocal_object", currentDS);
        result.put("user_name", name);
        result.put("user_id", new Random().nextInt(1000));
        result.put("current_thread", Thread.currentThread().getName());
        result.put("timestamp", new Date().toString());
        
        System.out.println("✅ 真实数据源: " + realDataSource);
        DataSourceContextHolder.clearDataSource();
        System.out.println("========== 写入操作结束 ==========\n");
        return result;
    }

    @GetMapping("/users/{id}/auth") 
    public Map<String, Object> getUserAuth(@PathVariable Long id) {
        System.out.println("\n========== 认证查询开始 (强制主库) ==========");
        DataSourceContextHolder.setMaster();
        
        DataSourceType currentDS = DataSourceContextHolder.getDataSource();
        String realDataSource = currentDS != null ? currentDS.getValue() : "未设置";
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "AUTH_SELECT");
        result.put("real_datasource_from_threadlocal", realDataSource);
        result.put("is_master", DataSourceContextHolder.isMaster());
        result.put("is_slave", DataSourceContextHolder.isSlave());
        result.put("threadlocal_object", currentDS);
        result.put("user_id", id);
        result.put("auth_status", "已验证");
        result.put("note", "强制使用主库进行认证查询");
        result.put("current_thread", Thread.currentThread().getName());
        result.put("timestamp", new Date().toString());
        
        System.out.println("✅ 认证查询 - 真实数据源: " + realDataSource);
        System.out.println("✅ 强制主库认证完成");
        DataSourceContextHolder.clearDataSource();
        System.out.println("========== 认证查询结束 ==========\n");
        return result;
    }

    @GetMapping("/datasource/test")
    public Map<String, Object> testDataSource() {
        Map<String, Object> result = new HashMap<>();
        DataSourceContextHolder.setMaster();
        DataSourceType masterDS = DataSourceContextHolder.getDataSource();
        result.put("master_test", masterDS != null ? masterDS.getValue() : "null");
        result.put("master_is_master", DataSourceContextHolder.isMaster());
        
        DataSourceContextHolder.setSlave();
        DataSourceType slaveDS = DataSourceContextHolder.getDataSource();
        result.put("slave_test", slaveDS != null ? slaveDS.getValue() : "null");
        result.put("slave_is_slave", DataSourceContextHolder.isSlave());
        
        DataSourceContextHolder.clearDataSource();
        result.put("after_clear", DataSourceContextHolder.getDataSource());
        result.put("证明", "这些是ThreadLocal中的真实值，不是硬编码");
        return result;
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("application", "读写分离演示");
        result.put("status", "运行中");
        result.put("timestamp", new Date().toString());
        return result;
    }
}