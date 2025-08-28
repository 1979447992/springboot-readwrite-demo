package com.demo.readwrite.mapper;

import com.demo.readwrite.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE id = #{id}")
    User selectById(Long id);

    @Select("SELECT * FROM users ORDER BY id LIMIT #{limit}")
    List<User> selectList(@Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM users")
    int count();

    @Insert("INSERT INTO users (username, email, age, status) VALUES (#{username}, #{email}, #{age}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE users SET username = #{username}, email = #{email}, age = #{age}, status = #{status} WHERE id = #{id}")
    int update(User user);

    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM users WHERE status = #{status} ORDER BY created_at DESC LIMIT #{limit}")
    List<User> selectByStatus(@Param("status") Integer status, @Param("limit") int limit);
}