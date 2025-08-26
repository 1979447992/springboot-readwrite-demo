package com.demo.readwrite;

import com.demo.readwrite.DataSourceContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class PostgreSQLTestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/users")
    public Map<String, Object> getUsers() {
        System.out.println("\n========== 查询操作开始 ==========");
        DataSourceContextHolder.setSlave();
        
        try {
            // 查询用户数据
            List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT * FROM users ORDER BY id");
            
            Map<String, Object> result = new HashMap<>();
            result.put("operation", "SELECT");
            result.put("datasource", "SLAVE数据库 (从库)");
            result.put("real_datasource_from_threadlocal", 
                DataSourceContextHolder.getDataSource() != null ? 
                DataSourceContextHolder.getDataSource().getValue() : "未设置");
            result.put("is_slave", DataSourceContextHolder.isSlave());
            result.put("users", users);
            result.put("count", users.size());
            result.put("timestamp", new Date().toString());
            
            System.out.println("✅ 从库查询完成，返回 " + users.size() + " 条记录");
            return result;
        } finally {
            DataSourceContextHolder.clearDataSource();
            System.out.println("========== 查询操作结束 ==========\n");
        }
    }

    @PostMapping("/users")
    public Map<String, Object> createUser(@RequestParam String username, 
                                         @RequestParam String email) {
        System.out.println("\n========== 写入操作开始 ==========");
        DataSourceContextHolder.setMaster();
        
        try {
            // 插入用户数据到主库
            String sql = "INSERT INTO users (username, email) VALUES (?, ?) RETURNING id, username, email, created_at";
            Map<String, Object> newUser = jdbcTemplate.queryForMap(sql, username, email);
            
            Map<String, Object> result = new HashMap<>();
            result.put("operation", "INSERT");
            result.put("datasource", "MASTER数据库 (主库)");
            result.put("real_datasource_from_threadlocal", 
                DataSourceContextHolder.getDataSource().getValue());
            result.put("is_master", DataSourceContextHolder.isMaster());
            result.put("user", newUser);
            result.put("timestamp", new Date().toString());
            result.put("sync_note", "数据将自动同步到从库，稍后可查询验证");
            
            System.out.println("✅ 主库写入成功: " + username + " (" + email + ")");
            return result;
        } finally {
            DataSourceContextHolder.clearDataSource();
            System.out.println("========== 写入操作结束 ==========\n");
        }
    }

    @PostMapping("/payments")
    public Map<String, Object> createPayment(@RequestParam Long userId,
                                            @RequestParam Double amount,
                                            @RequestParam String description) {
        System.out.println("\n========== 支付操作开始 (强制主库) ==========");
        DataSourceContextHolder.setMaster();
        
        try {
            String sql = "INSERT INTO payments (user_id, amount, description) VALUES (?, ?, ?) RETURNING *";
            Map<String, Object> payment = jdbcTemplate.queryForMap(sql, userId, amount, description);
            
            Map<String, Object> result = new HashMap<>();
            result.put("operation", "PAYMENT_INSERT");
            result.put("datasource", "MASTER数据库 (支付必须主库)");
            result.put("real_datasource_from_threadlocal", 
                DataSourceContextHolder.getDataSource().getValue());
            result.put("is_master", DataSourceContextHolder.isMaster());
            result.put("payment", payment);
            result.put("timestamp", new Date().toString());
            
            System.out.println("✅ 支付记录写入主库成功: " + amount + " 元");
            return result;
        } finally {
            DataSourceContextHolder.clearDataSource();
            System.out.println("========== 支付操作结束 ==========\n");
        }
    }

    @GetMapping("/payments")
    public Map<String, Object> getPayments() {
        System.out.println("\n========== 支付查询开始 (从库) ==========");
        DataSourceContextHolder.setSlave();
        
        try {
            String sql = "SELECT p.*, u.username FROM payments p JOIN users u ON p.user_id = u.id ORDER BY p.id";
            List<Map<String, Object>> payments = jdbcTemplate.queryForList(sql);
            
            Map<String, Object> result = new HashMap<>();
            result.put("operation", "PAYMENT_SELECT");
            result.put("datasource", "SLAVE数据库 (从库)");
            result.put("real_datasource_from_threadlocal", 
                DataSourceContextHolder.getDataSource().getValue());
            result.put("is_slave", DataSourceContextHolder.isSlave());
            result.put("payments", payments);
            result.put("count", payments.size());
            result.put("timestamp", new Date().toString());
            
            System.out.println("✅ 从库查询支付记录: " + payments.size() + " 条");
            return result;
        } finally {
            DataSourceContextHolder.clearDataSource();
            System.out.println("========== 支付查询结束 ==========\n");
        }
    }

    @GetMapping("/sync-test")
    public Map<String, Object> testSync() {
        Map<String, Object> result = new HashMap<>();
        
        // 从主库查询
        DataSourceContextHolder.setMaster();
        int masterCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        DataSourceContextHolder.clearDataSource();
        
        // 从从库查询
        DataSourceContextHolder.setSlave();  
        int slaveCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        DataSourceContextHolder.clearDataSource();
        
        result.put("master_count", masterCount);
        result.put("slave_count", slaveCount);
        result.put("sync_status", masterCount == slaveCount ? "同步正常" : "同步延迟");
        result.put("sync_note", "主从数据库记录数量对比");
        
        return result;
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("application", "PostgreSQL读写分离演示");
        result.put("master_port", "5432");
        result.put("slave_port", "5433");
        result.put("status", "运行中");
        result.put("timestamp", new Date().toString());
        result.put("test_apis", Arrays.asList(
            "GET /api/users - 查询用户(从库)",
            "POST /api/users - 创建用户(主库)", 
            "GET /api/payments - 查询支付(从库)",
            "POST /api/payments - 创建支付(主库)",
            "GET /api/sync-test - 测试主从同步"
        ));
        return result;
    }
}