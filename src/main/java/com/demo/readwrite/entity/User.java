package com.demo.readwrite.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("demo_user")
public class User {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String username;
    
    private String email;
    
    private Integer age;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time") 
    private LocalDateTime updateTime;

    public User() {}

    public User(String username, String email, Integer age) {
        this.username = username;
        this.email = email;
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}