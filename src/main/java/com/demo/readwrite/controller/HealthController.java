package com.demo.readwrite.controller;

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
        result.put("application", "ReadWrite Demo with ShardingSphere");
        result.put("version", "1.0.0");
        result.put("timestamp", System.currentTimeMillis());
        result.put("datasource", "ShardingSphere读写分离");
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/datasource")
    public ResponseEntity<Map<String, Object>> getDataSourceInfo() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("framework", "ShardingSphere 5.1.1");
        result.put("readwriteSplitting", "enabled");
        result.put("masterDataSource", "oracle-db:1521/XEPDB1 (MASTER_USER)");
        result.put("slaveDataSource", "oracle-db:1521/XEPDB1 (SLAVE_USER)");
        result.put("timestamp", System.currentTimeMillis());
        result.put("note", "使用ShardingSphere自动路由，写操作→主库，读操作→从库");
        
        return ResponseEntity.ok(result);
    }
}