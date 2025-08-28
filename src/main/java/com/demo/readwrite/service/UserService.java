package com.demo.readwrite.service;

import com.demo.readwrite.entity.User;
import com.demo.readwrite.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * 用户业务Service - 使用ShardingSphere自动读写分离
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    
    /**
     * 查询所有用户 - 自动路由到SLAVE
     */
    public List<User> findAllUsers() {
        System.out.println("📖 [SLAVE-DB] 查询所有用户 - ShardingSphere自动路由到从库");
        return userMapper.selectList(100);
    }
    
    /**
     * 根据ID查询用户 - 自动路由到SLAVE  
     */
    public User findUserById(Long id) {
        System.out.println("📖 [SLAVE-DB] 根据ID查询用户: " + id + " - ShardingSphere自动路由到从库");
        return userMapper.selectById(id);
    }
    
    /**
     * 创建用户 - 自动路由到MASTER
     */
    @Transactional
    public User createUser(String username, String email, Integer age) {
        System.out.println("✍️ [MASTER-DB] 创建用户: " + username + " - ShardingSphere自动路由到主库");
        User user = new User(username, email, age);
        userMapper.insert(user);
        return user;
    }
    
    /**
     * 更新用户 - 自动路由到MASTER
     */
    @Transactional
    public User updateUser(Long id, String username, String email, Integer age) {
        System.out.println("✍️ [MASTER-DB] 更新用户: " + id + " - ShardingSphere自动路由到主库");
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setAge(age);
        user.setStatus(1);
        userMapper.update(user);
        return user;
    }
    
    /**
     * 删除用户 - 自动路由到MASTER
     */
    @Transactional
    public int deleteUser(Long id) {
        System.out.println("✍️ [MASTER-DB] 删除用户: " + id + " - ShardingSphere自动路由到主库");
        return userMapper.deleteById(id);
    }
    
    /**
     * 统计用户总数 - 自动路由到SLAVE
     */
    public int countUsers() {
        System.out.println("📖 [SLAVE-DB] 统计用户总数 - ShardingSphere自动路由到从库");
        return userMapper.count();
    }
    
    /**
     * 根据用户名查询用户 - 自动路由到SLAVE
     */
    public User findUserByUsername(String username) {
        System.out.println("📖 [SLAVE-DB] 根据用户名查询用户: " + username + " - ShardingSphere自动路由到从库");
        return userMapper.selectByUsername(username);
    }
    
    /**
     * 根据状态查询用户 - 自动路由到SLAVE
     */
    public List<User> findUsersByStatus(Integer status) {
        System.out.println("📖 [SLAVE-DB] 根据状态查询用户: " + status + " - ShardingSphere自动路由到从库");
        return userMapper.selectByStatus(status, 50);
    }
}