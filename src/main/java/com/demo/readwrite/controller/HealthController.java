package com.demo.readwrite.controller;

import com.demo.readwrite.routing.DataSourceContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("application", "ReadWrite Demo");
        result.put("version", "1.0.0");
        result.put("timestamp", System.currentTimeMillis());
        
        String currentDataSource = DataSourceContextHolder.getDataSource() != null 
            ? DataSourceContextHolder.getDataSource().getValue() 
            : "未设置";
        result.put("currentDataSource", currentDataSource);
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/datasource")
    public ResponseEntity<Map<String, Object>> getDataSourceInfo() {
        Map<String, Object> result = new HashMap<>();
        
        String currentDataSource = DataSourceContextHolder.getDataSource() != null 
            ? DataSourceContextHolder.getDataSource().getValue() 
            : "slave";
        
        result.put("currentDataSource", currentDataSource);
        result.put("isMaster", DataSourceContextHolder.isMaster());
        result.put("isSlave", DataSourceContextHolder.isSlave());
        result.put("timestamp", System.currentTimeMillis());
        result.put("note", "这是一个测试接口，用于检查当前数据源路由状态");
        
        return ResponseEntity.ok(result);
    }
}