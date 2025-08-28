package com.demo.readwrite;

import com.demo.readwrite.entity.User;
import com.demo.readwrite.service.UserService;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * 读写分离测试Controller - 使用ShardingSphere自动读写分离
 */
@RestController
@RequestMapping("/api")
public class ReadWriteController {

    @Autowired
    private UserService userService;

    /**
     * 查询所有用户 - ShardingSphere自动路由到从库
     */
    @GetMapping("/users")
    public Map<String, Object> getUsers() {
        System.out.println("\n========== 查询所有用户操作开始 ==========");
        
        List<User> users = userService.findAllUsers();
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "SELECT_ALL_USERS");
        result.put("routing", "ShardingSphere自动路由到从库");
        result.put("users", users);
        result.put("count", users.size());
        result.put("timestamp", new Date().toString());
        
        System.out.println("========== 查询所有用户操作结束 ==========\n");
        return result;
    }

    /**
     * 根据ID查询用户 - ShardingSphere自动路由到从库
     */
    @GetMapping("/users/{id}")
    public Map<String, Object> getUserById(@PathVariable Long id) {
        System.out.println("\n========== 根据ID查询用户操作开始 ==========");
        
        User user = userService.findUserById(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "SELECT_USER_BY_ID");
        result.put("routing", "ShardingSphere自动路由到从库");
        result.put("user_id", id);
        result.put("user", user);
        result.put("found", user != null);
        result.put("timestamp", new Date().toString());
        
        System.out.println("========== 根据ID查询用户操作结束 ==========\n");
        return result;
    }

    /**
     * 创建用户 - ShardingSphere自动路由到主库
     */
    @PostMapping("/users")
    public Map<String, Object> createUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam(defaultValue = "25") Integer age) {
        System.out.println("\n========== 创建用户操作开始 ==========");
        
        User user = userService.createUser(username, email, age);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "INSERT_USER");
        result.put("routing", "ShardingSphere自动路由到主库");
        result.put("user", user);
        result.put("timestamp", new Date().toString());
        
        System.out.println("========== 创建用户操作结束 ==========\n");
        return result;
    }

    /**
     * 更新用户 - ShardingSphere自动路由到主库
     */
    @PutMapping("/users/{id}")
    public Map<String, Object> updateUser(
            @PathVariable Long id,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam Integer age) {
        System.out.println("\n========== 更新用户操作开始 ==========");
        
        User user = userService.updateUser(id, username, email, age);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "UPDATE_USER");
        result.put("routing", "ShardingSphere自动路由到主库");
        result.put("user_id", id);
        result.put("user", user);
        result.put("timestamp", new Date().toString());
        
        System.out.println("========== 更新用户操作结束 ==========\n");
        return result;
    }

    /**
     * 删除用户 - ShardingSphere自动路由到主库
     */
    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        System.out.println("\n========== 删除用户操作开始 ==========");
        
        int rows = userService.deleteUser(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "DELETE_USER");
        result.put("routing", "ShardingSphere自动路由到主库");
        result.put("user_id", id);
        result.put("affected_rows", rows);
        result.put("deleted", rows > 0);
        result.put("timestamp", new Date().toString());
        
        System.out.println("========== 删除用户操作结束 ==========\n");
        return result;
    }

    /**
     * 强制主库查询 - 使用HintManager强制路由到主库
     */
    @GetMapping("/users/{id}/auth")
    public Map<String, Object> getUserAuthFromMaster(@PathVariable Long id) {
        System.out.println("\n========== 强制主库认证查询开始 ==========");
        
        User user;
        try (HintManager hintManager = HintManager.getInstance()) {
            // 强制使用主库
            hintManager.setWriteRouteOnly();
            user = userService.findUserById(id);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "AUTH_SELECT_FROM_MASTER");
        result.put("routing", "HintManager强制路由到主库");
        result.put("user_id", id);
        result.put("user", user);
        result.put("note", "强制使用主库进行认证查询，保证数据实时性");
        result.put("timestamp", new Date().toString());
        
        System.out.println("========== 强制主库认证查询结束 ==========\n");
        return result;
    }

    /**
     * 统计用户总数 - ShardingSphere自动路由到从库
     */
    @GetMapping("/users/count")
    public Map<String, Object> countUsers() {
        System.out.println("\n========== 统计用户总数开始 ==========");
        
        int count = userService.countUsers();
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "COUNT_USERS");
        result.put("routing", "ShardingSphere自动路由到从库");
        result.put("total_users", count);
        result.put("timestamp", new Date().toString());
        
        System.out.println("========== 统计用户总数结束 ==========\n");
        return result;
    }

    /**
     * 根据用户名查询用户 - ShardingSphere自动路由到从库
     */
    @GetMapping("/users/search")
    public Map<String, Object> searchUserByUsername(@RequestParam String username) {
        System.out.println("\n========== 根据用户名搜索用户开始 ==========");
        
        User user = userService.findUserByUsername(username);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "SEARCH_USER_BY_USERNAME");
        result.put("routing", "ShardingSphere自动路由到从库");
        result.put("username", username);
        result.put("user", user);
        result.put("found", user != null);
        result.put("timestamp", new Date().toString());
        
        System.out.println("========== 根据用户名搜索用户结束 ==========\n");
        return result;
    }

    /**
     * 应用状态检查
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("application", "SpringBoot + ShardingSphere 读写分离演示");
        result.put("version", "2.0-MySQL");
        result.put("database", "MySQL 8.0 (主从复制)");
        result.put("sharding_sphere", "5.4.1");
        result.put("features", Arrays.asList("读写分离", "@DS注解混合", "强制主库查询"));
        result.put("status", "运行中");
        result.put("timestamp", new Date().toString());
        return result;
    }
}