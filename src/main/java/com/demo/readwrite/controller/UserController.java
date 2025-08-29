package com.demo.readwrite.controller;

import com.demo.readwrite.entity.User;
import com.demo.readwrite.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 创建用户 (写操作 - 路由到主库)
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestParam String username,
                                         @RequestParam String email,
                                         @RequestParam Integer age) {
        User savedUser = userService.createUser(username, email, age);
        return ResponseEntity.ok(savedUser);
    }

    /**
     * 更新用户 (写操作 - 路由到主库)
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                         @RequestBody User user) {
        user.setId(id);
        User updatedUser = userService.updateUser(user);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * 删除用户 (写操作 - 路由到主库)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteUser(@PathVariable Long id) {
        boolean result = userService.removeById(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据ID查询用户 (读操作 - 路由到从库)
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    /**
     * 查询所有用户 (读操作 - 路由到从库)
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.list();
        return ResponseEntity.ok(users);
    }

    /**
     * 根据用户名查询 (读操作 - 路由到从库)
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<List<User>> getUserByUsername(@PathVariable String username) {
        List<User> users = userService.findByUsername(username);
        return ResponseEntity.ok(users);
    }

    /**
     * 测试强制主库路由
     */
    @GetMapping("/master-test/{id}")
    public ResponseEntity<User> getUserFromMaster(@PathVariable Long id) {
        User user = userService.getFromMaster(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    /**
     * 读写分离验证接口
     */
    @GetMapping("/test-routing")
    public ResponseEntity<String> testRouting() {
        return ResponseEntity.ok("读写分离测试接口 - 查看控制台日志验证路由情况");
    }
}