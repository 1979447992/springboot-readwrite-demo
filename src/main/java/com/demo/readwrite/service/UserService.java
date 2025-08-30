package com.demo.readwrite.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.readwrite.annotation.MasterOnly;
import com.demo.readwrite.entity.User;
import com.demo.readwrite.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务 - 演示Dynamic DataSource智能读写分离
 * 
 * 核心功能：
 * 1. 自动读写分离：写操作->主库，读操作->从库
 * 2. @MasterOnly强制主库：特殊场景强制读主库
 * 3. 事务强制主库：@Transactional中所有操作走主库
 */
@Service
@Slf4j
public class UserService extends ServiceImpl<UserMapper, User> {

    /**
     * 创建用户 (写操作，自动路由到主库)
     */
    public User createUser(String username, String email, Integer age) {
        log.info("创建用户: {} - 自动路由到主库", username);
        User user = new User(username, email, age);
        save(user);
        return user;
    }

    /**
     * 查询所有用户 (读操作，自动路由到从库)
     */
    public List<User> getAllUsers() {
        log.info("查询所有用户 - 自动路由到从库（K8s负载均衡）");
        return list();
    }

    /**
     * 根据ID查询用户 (读操作，自动路由到从库)
     */
    public User getUserById(Long id) {
        log.info("根据ID查询用户: {} - 自动路由到从库", id);
        return getById(id);
    }
    
    /**
     * 强制从主库查询用户 (使用@MasterOnly注解)
     * 适用于对数据一致性要求极高的场景
     */
    @MasterOnly("查询关键用户信息需要最新数据")
    public User getUserFromMaster(Long id) {
        log.info("从主库查询用户: {} - @MasterOnly强制主库", id);
        return getById(id);
    }

    /**
     * 更新用户 (写操作，自动路由到主库)
     */
    public boolean updateUser(User user) {
        log.info("更新用户: {} - 自动路由到主库", user.getUsername());
        return updateById(user);
    }

    /**
     * 删除用户 (写操作，自动路由到主库)
     */
    public boolean deleteUser(Long id) {
        log.info("删除用户: {} - 自动路由到主库", id);
        return removeById(id);
    }
    
    /**
     * 事务中的复合操作 (事务中所有操作强制主库)
     */
    @Transactional
    public User createAndQuery(String username, String email, Integer age) {
        log.info("事务操作开始 - 所有操作强制主库");
        
        // 创建用户（主库）
        User user = new User(username, email, age);
        save(user);
        
        // 立即查询（主库，保证读取到最新数据）
        User savedUser = getById(user.getId());
        
        log.info("事务操作完成 - 用户ID: {}", savedUser.getId());
        return savedUser;
    }
    
    /**
     * 带锁的查询操作（自动识别FOR UPDATE，路由到主库）
     */
    public User getUserForUpdate(Long id) {
        log.info("加锁查询用户: {} - FOR UPDATE自动路由到主库", id);
        return baseMapper.selectForUpdate(id);
    }
    
    /**
     * 演示复合查询场景
     */
    public User getAndRefreshUser(Long id) {
        log.info("复合查询场景开始 - ID: {}", id);
        
        // 普通查询（从库）
        User user = getById(id);
        if (user == null) {
            return null;
        }
        
        // 需要最新数据的查询（强制主库）
        user = getUserFromMaster(id);
        
        log.info("复合查询完成 - 最终使用主库数据");
        return user;
    }
}
