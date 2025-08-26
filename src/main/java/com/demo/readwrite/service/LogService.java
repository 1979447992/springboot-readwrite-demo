package com.demo.readwrite.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 日志服务 - 使用@DS("log")指定日志数据库
 * 不进行读写分离，直接使用单一日志库
 */
@Service
public class LogService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 写入操作日志 - 使用@DS指定日志库
     * 即使方法名是write开头，也不会触发读写分离
     */
    @DS("log")
    public Map<String, Object> writeLog(String message) {
        System.out.println("📝 [LOG-DB] 写入日志库 - 使用 @DS(\"log\")");
        
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("id", new Random().nextInt(10000));
        logEntry.put("message", message);
        logEntry.put("level", "INFO");
        logEntry.put("datasource", "log库 (localhost:5435)");
        logEntry.put("note", "即使是write操作，也不走读写分离，直接用日志库");
        logEntry.put("timestamp", new Date().toString());
        
        // 注意：这里会使用localhost:5435的log数据库
        // 不会触发master/slave的读写分离逻辑
        
        return logEntry;
    }
    
    /**
     * 查询操作日志 - 使用@DS指定日志库
     */
    @DS("log")
    public List<Map<String, Object>> getLogs() {
        System.out.println("📝 [LOG-DB] 查询日志库 - 使用 @DS(\"log\")");
        
        // 模拟日志查询
        List<Map<String, Object>> logs = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> log = new HashMap<>();
            log.put("id", i);
            log.put("message", "系统日志 " + i);
            log.put("level", "INFO");
            log.put("timestamp", new Date().toString());
            logs.add(log);
        }
        
        return logs;
    }
}