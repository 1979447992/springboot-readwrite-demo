package com.demo.readwrite.service;

import com.demo.readwrite.entity.User;
import com.demo.readwrite.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @MockBean
    private UserMapper userMapper;

    private UserService userService;
    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        userService.setBaseMapper(userMapper);
        
        testUser = new User("testuser", "test@example.com", "13800000001", "password123");
        testUser.setId(1001L);
    }

    @Test
    void createUser_ShouldReturnUser_WhenValidData() {
        when(userMapper.insert(any(User.class))).thenReturn(1);
        
        User result = userService.createUser("testuser", "test@example.com", "13800000001", "password123");
        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("13800000001", result.getPhone());
        assertEquals("password123", result.getPassword());
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        when(userMapper.selectById(1001L)).thenReturn(testUser);
        
        User result = userService.getUserById(1001L);
        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userMapper, times(1)).selectById(1001L);
    }

    @Test
    void getUserById_ShouldReturnNull_WhenUserNotExists() {
        when(userMapper.selectById(9999L)).thenReturn(null);
        
        User result = userService.getUserById(9999L);
        
        assertNull(result);
        verify(userMapper, times(1)).selectById(9999L);
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenUsernameExists() {
        when(userMapper.findByUsername("testuser")).thenReturn(testUser);
        
        User result = userService.findByUsername("testuser");
        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userMapper, times(1)).findByUsername("testuser");
    }

    @Test
    void authenticate_ShouldReturnUser_WhenCredentialsValid() {
        when(userMapper.findByUsername("testuser")).thenReturn(testUser);
        
        User result = userService.authenticate("testuser", "password123");
        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userMapper, times(1)).findByUsername("testuser");
    }

    @Test
    void authenticate_ShouldReturnNull_WhenPasswordInvalid() {
        when(userMapper.findByUsername("testuser")).thenReturn(testUser);
        
        User result = userService.authenticate("testuser", "wrongpassword");
        
        assertNull(result);
        verify(userMapper, times(1)).findByUsername("testuser");
    }

    @Test
    void authenticate_ShouldReturnNull_WhenUserNotExists() {
        when(userMapper.findByUsername("nonexistentuser")).thenReturn(null);
        
        User result = userService.authenticate("nonexistentuser", "password123");
        
        assertNull(result);
        verify(userMapper, times(1)).findByUsername("nonexistentuser");
    }

    @Test
    void findByStatus_ShouldReturnUsers_WhenUsersExist() {
        List<User> users = Arrays.asList(testUser);
        when(userMapper.findByStatus(1)).thenReturn(users);
        
        List<User> result = userService.findByStatus(1);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        verify(userMapper, times(1)).findByStatus(1);
    }
}