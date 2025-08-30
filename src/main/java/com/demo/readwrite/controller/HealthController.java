package com.demo.readwrite.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查Controller - 简化版
 */
@Slf4j
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        log.info("健康检查请求");
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", LocalDateTime.now());
        result.put("service", "readwrite-demo");
        result.put("message", "读写分离服务运行正常");
        
        return ResponseEntity.ok(result);
    }
}
