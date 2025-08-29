package com.demo.readwrite.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.readwrite.annotation.MasterDB;
import com.demo.readwrite.entity.User;
import com.demo.readwrite.mapper.UserMapper;
import com.demo.readwrite.config.MasterRouteManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Autowired
    private MasterRouteManager masterRouteManager;

    public User createUser(String username, String email, Integer age) {
        User user = new User(username, email, age);
        save(user);
        return user;
    }

    public User updateUser(User user) {
        updateById(user);
        return user;
    }

    public boolean deleteUser(Long id) {
        return removeById(id);
    }

    public User getUserById(Long id) {
        return getById(id);
    }

    public List<User> getAllUsers() {
        return list();
    }

    public List<User> findByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return list(queryWrapper);
    }

    /**
     * 强制从主库查询用户信息
     */
    @MasterDB("强制主库查询")
    public User getFromMaster(Long id) {
        return getById(id);
    }

    /**
     * 另一种实现强制主库查询的方式
     */
    public User getFromMasterAlternative(Long id) {
        return masterRouteManager.executeOnMaster(() -> getById(id));
    }

    @MasterDB("用户状态变更需要强制主库查询")
    public List<User> findByStatus(Integer status) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", status);
        return list(queryWrapper);
    }

    public List<User> searchUsers(String keyword) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("username", keyword)
                   .or()
                   .like("email", keyword);
        return list(queryWrapper);
    }
}