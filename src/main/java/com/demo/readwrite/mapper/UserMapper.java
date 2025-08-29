package com.demo.readwrite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.readwrite.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM users WHERE username = #{username} AND deleted = 0")
    User findByUsername(@Param("username") String username);

    @Select("SELECT * FROM users WHERE email = #{email} AND deleted = 0")
    User findByEmail(@Param("email") String email);

    @Select("SELECT * FROM users WHERE phone = #{phone} AND deleted = 0")
    User findByPhone(@Param("phone") String phone);

    @Select("SELECT * FROM users WHERE status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<User> findByStatus(@Param("status") Integer status);
}