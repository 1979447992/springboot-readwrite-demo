package com.demo.readwrite.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.readwrite.annotation.MasterDB;
import com.demo.readwrite.entity.User;
import com.demo.readwrite.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    public User createUser(String username, String email, String phone, String password) {
        User user = new User(username, email, phone, password);
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

    public User findByUsername(String username) {
        return baseMapper.findByUsername(username);
    }

    public User findByEmail(String email) {
        return baseMapper.findByEmail(email);
    }

    public User findByPhone(String phone) {
        return baseMapper.findByPhone(phone);
    }

    @MasterDB("用户状态变更需要强制主库查询")
    public List<User> findByStatus(Integer status) {
        return baseMapper.findByStatus(status);
    }

    @MasterDB("用户认证需要强制主库查询")
    public User authenticate(String username, String password) {
        User user = findByUsername(username);
        if (user != null && password.equals(user.getPassword())) {
            return user;
        }
        return null;
    }

    public List<User> searchUsers(String keyword) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("username", keyword)
                   .or()
                   .like("email", keyword)
                   .or()
                   .like("phone", keyword);
        return list(queryWrapper);
    }
}