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
     * 写入应用日志 - 使用@DS指定日志库
     */
    @DS("log")
    public Map<String, Object> writeApplicationLog(String logLevel, String loggerName, String message, String threadName, Long userId, String sessionId) {
        System.out.println("📝 [LOG-DB] 写入应用日志 - 使用 @DS(\"log\") - localhost:5435");
        
        String sql = "INSERT INTO application_logs (log_level, logger_name, message, thread_name, user_id, session_id) VALUES (?, ?, ?, ?, ?, ?)";
        int rows = jdbcTemplate.update(sql, logLevel, loggerName, message, threadName, userId, sessionId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "WRITE_APPLICATION_LOG");
        result.put("level", logLevel);
        result.put("message", message);
        result.put("affected_rows", rows);
        result.put("datasource", "log库 (localhost:5435)");
        result.put("timestamp", new Date().toString());
        
        return result;
    }
    
    /**
     * 写入审计日志 - 使用@DS指定日志库
     */
    @DS("log")
    public Map<String, Object> writeAuditLog(Long userId, String username, String action, String resource, String resourceId, Object oldValue, Object newValue, String ipAddress) {
        System.out.println("📝 [LOG-DB] 写入审计日志 - 使用 @DS(\"log\")");
        
        String sql = "INSERT INTO audit_logs (user_id, username, action, resource, resource_id, old_value, new_value, ip_address) VALUES (?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::inet)";
        String oldJson = oldValue != null ? oldValue.toString() : null;
        String newJson = newValue != null ? newValue.toString() : null;
        
        int rows = jdbcTemplate.update(sql, userId, username, action, resource, resourceId, oldJson, newJson, ipAddress);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "WRITE_AUDIT_LOG");
        result.put("action", action);
        result.put("resource", resource);
        result.put("affected_rows", rows);
        result.put("datasource", "log库 (localhost:5435)");
        result.put("timestamp", new Date().toString());
        
        return result;
    }
    
    /**
     * 查询应用日志 - 使用@DS指定日志库
     */
    @DS("log")
    public List<Map<String, Object>> getApplicationLogs(String logLevel, int limit) {
        System.out.println("📝 [LOG-DB] 查询应用日志 - 使用 @DS(\"log\")");
        
        String sql;
        Object[] params;
        
        if (logLevel != null && !logLevel.trim().isEmpty()) {
            sql = "SELECT * FROM application_logs WHERE log_level = ? ORDER BY timestamp DESC LIMIT ?";
            params = new Object[]{logLevel, limit};
        } else {
            sql = "SELECT * FROM application_logs ORDER BY timestamp DESC LIMIT ?";
            params = new Object[]{limit};
        }
        
        return jdbcTemplate.queryForList(sql, params);
    }
    
    /**
     * 查询审计日志 - 使用@DS指定日志库
     */
    @DS("log")
    public List<Map<String, Object>> getAuditLogs(Long userId, String action, int limit) {
        System.out.println("📝 [LOG-DB] 查询审计日志 - 使用 @DS(\"log\")");
        
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_logs WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (userId != null) {
            sql.append(" AND user_id = ?");
            params.add(userId);
        }
        
        if (action != null && !action.trim().isEmpty()) {
            sql.append(" AND action = ?");
            params.add(action);
        }
        
        sql.append(" ORDER BY timestamp DESC LIMIT ?");
        params.add(limit);
        
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }
    
    /**
     * 记录系统指标 - 使用@DS指定日志库
     */
    @DS("log")
    public Map<String, Object> recordSystemMetric(String metricName, Double metricValue, String metricUnit, Map<String, Object> tags) {
        System.out.println("📝 [LOG-DB] 记录系统指标: " + metricName + " - 使用 @DS(\"log\")");
        
        String tagsJson = tags != null ? tags.toString() : null;
        String sql = "INSERT INTO system_metrics (metric_name, metric_value, metric_unit, tags) VALUES (?, ?, ?, ?::jsonb)";
        int rows = jdbcTemplate.update(sql, metricName, metricValue, metricUnit, tagsJson);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "RECORD_METRIC");
        result.put("metric", metricName);
        result.put("value", metricValue);
        result.put("affected_rows", rows);
        result.put("datasource", "log库 (localhost:5435)");
        result.put("timestamp", new Date().toString());
        
        return result;
    }
    
    /**
     * 获取系统指标 - 使用@DS指定日志库
     */
    @DS("log")
    public List<Map<String, Object>> getSystemMetrics(String metricName, int hours) {
        System.out.println("📝 [LOG-DB] 查询系统指标: " + metricName + " - 使用 @DS(\"log\")");
        
        String sql = "SELECT * FROM system_metrics WHERE metric_name = ? AND timestamp >= NOW() - INTERVAL ? HOUR ORDER BY timestamp DESC";
        return jdbcTemplate.queryForList(sql, metricName, hours);
    }
    
    /**
     * 删除旧日志 (物理删除)
     */
    @DS("log")
    public Map<String, Object> cleanupOldLogs(int daysToKeep) {
        System.out.println("📝 [LOG-DB] 清理旧日志 - 使用 @DS(\"log\")");
        
        String[] tables = {"application_logs", "audit_logs", "system_metrics"};
        int totalDeleted = 0;
        
        for (String table : tables) {
            String sql = "DELETE FROM " + table + " WHERE timestamp < NOW() - INTERVAL ? DAY";
            int rows = jdbcTemplate.update(sql, daysToKeep);
            totalDeleted += rows;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "CLEANUP_LOGS");
        result.put("days_kept", daysToKeep);
        result.put("total_deleted", totalDeleted);
        result.put("datasource", "log库 (localhost:5435)");
        result.put("timestamp", new Date().toString());
        
        return result;
    }
}