package com.demo.readwrite.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;  
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 主业务Service - 使用默认数据源，自动读写分离
 */
@Service
public class UserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 查询所有用户 - 自动路由到SLAVE
     */
    public List<Map<String, Object>> findAllUsers() {
        return jdbcTemplate.queryForList("SELECT * FROM users ORDER BY id");
    }
    
    /**
     * 创建用户 - 自动路由到MASTER
     */
    public Map<String, Object> createUser(String username, String email) {
        String sql = "INSERT INTO users (username, email) VALUES (?, ?) RETURNING *";
        return jdbcTemplate.queryForMap(sql, username, email);
    }
    
    /**
     * 更新用户 - 自动路由到MASTER
     */
    public Map<String, Object> updateUser(Long id, String username, String email) {
        String sql = "UPDATE users SET username=?, email=?, updated_at=NOW() WHERE id=? RETURNING *";
        return jdbcTemplate.queryForMap(sql, username, email, id);
    }
    
    /**
     * 强制从主库查询用户数量 - 用于同步测试
     */
    public int countUsersFromMaster() {
        // 这个方法名不会触发自动分离，但我们可以手动指定
        com.demo.readwrite.DataSourceContextHolder.setMaster();
        try {
            return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        } finally {
            com.demo.readwrite.DataSourceContextHolder.clearDataSource();
        }
    }
    
    /**
     * 强制从从库查询用户数量 - 用于同步测试
     */
    public int countUsersFromSlave() {
        com.demo.readwrite.DataSourceContextHolder.setSlave();
        try {
            return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        } finally {
            com.demo.readwrite.DataSourceContextHolder.clearDataSource();
        }
    }
}