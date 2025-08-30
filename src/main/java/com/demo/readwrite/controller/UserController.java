package com.demo.readwrite.controller;

import com.demo.readwrite.entity.User;
import com.demo.readwrite.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理Controller - 演示Dynamic DataSource读写分离
 * 
 * 测试核心功能：
 * 1. 主库从库读写分离
 * 2. @MasterOnly强制读主库
 */
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 创建用户 (写操作，自动路由到主库)
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestParam String username,
                                         @RequestParam String email,
                                         @RequestParam Integer age) {
        log.info("创建用户API调用: {} - 将自动路由到主库", username);
        User savedUser = userService.createUser(username, email, age);
        return ResponseEntity.ok(savedUser);
    }

    /**
     * 查询所有用户 (读操作，自动路由到从库)
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("查询所有用户API调用 - 将自动路由到从库");
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 根据ID查询用户 (读操作，自动路由到从库)
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("查询用户API调用: {} - 将自动路由到从库", id);
        User user = userService.getUserById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }
    
    /**
     * 强制从主库查询用户 (@MasterOnly注解强制主库)
     * GET /api/users/{id}/master
     */
    @GetMapping("/{id}/master")
    public ResponseEntity<User> getUserFromMaster(@PathVariable Long id) {
        log.info("强制主库查询用户API调用: {} - @MasterOnly强制主库", id);
        User user = userService.getUserFromMaster(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    /**
     * 更新用户 (写操作，自动路由到主库)
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        log.info("更新用户API调用: {} - 将自动路由到主库", id);
        user.setId(id);
        boolean updated = userService.updateUser(user);
        return updated ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    /**
     * 删除用户 (写操作，自动路由到主库)
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteUser(@PathVariable Long id) {
        log.info("删除用户API调用: {} - 将自动路由到主库", id);
        boolean result = userService.deleteUser(id);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 事务操作示例 (事务中所有操作强制主库)
     * POST /api/users/transaction
     */
    @PostMapping("/transaction")
    public ResponseEntity<User> createAndQuery(@RequestParam String username,
                                             @RequestParam String email,
                                             @RequestParam Integer age) {
        log.info("事务操作API调用: {} - 事务中所有操作强制主库", username);
        User user = userService.createAndQuery(username, email, age);
        return ResponseEntity.ok(user);
    }
    
    /**
     * 带锁查询 (FOR UPDATE自动路由到主库)
     * GET /api/users/{id}/lock
     */
    @GetMapping("/{id}/lock")
    public ResponseEntity<User> getUserForUpdate(@PathVariable Long id) {
        log.info("带锁查询API调用: {} - FOR UPDATE自动路由到主库", id);
        User user = userService.getUserForUpdate(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }
    
    /**
     * 复合查询场景
     * GET /api/users/{id}/refresh
     */
    @GetMapping("/{id}/refresh")
    public ResponseEntity<User> getAndRefreshUser(@PathVariable Long id) {
        log.info("复合查询API调用: {} - 先从库后主库", id);
        User user = userService.getAndRefreshUser(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }
    
    /**
     * 读写分离功能演示
     * GET /api/users/demo
     */
    @GetMapping("/demo")
    public ResponseEntity<Map<String, Object>> readWriteSplitDemo() {
        log.info("=== 读写分离功能演示开始 ===");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 测试读操作（从库）
            List<User> allUsers = userService.getAllUsers();
            result.put("read_operation", "查询所有用户，路由到从库");
            result.put("user_count", allUsers.size());
            
            // 2. 测试@MasterOnly强制主库
            if (!allUsers.isEmpty()) {
                User masterUser = userService.getUserFromMaster(allUsers.get(0).getId());
                result.put("master_only", "@MasterOnly强制主库查询");
                result.put("master_user", masterUser != null ? masterUser.getUsername() : "null");
            }
            
            result.put("status", "success");
            result.put("message", "请查看控制台日志验证各个数据源的路由情况");
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
        }
        
        log.info("=== 读写分离功能演示结束 ===");
        return ResponseEntity.ok(result);
    }
}
