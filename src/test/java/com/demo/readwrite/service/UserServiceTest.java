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
        
        testUser = new User("testuser", "test@example.com", 25);
        testUser.setId(1001L);
    }

    @Test
    void createUser_ShouldReturnUser_WhenValidData() {
        when(userMapper.insert(any(User.class))).thenReturn(1);
        
        User result = userService.createUser("testuser", "test@example.com", 25);
        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(25, result.getAge());
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
    void findByUsername_ShouldReturnUserList_WhenUsernameExists() {
        List<User> users = Arrays.asList(testUser);
        when(userMapper.selectList(any())).thenReturn(users);
        
        List<User> result = userService.findByUsername("testuser");
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        verify(userMapper, times(1)).selectList(any());
    }

    @Test
    void getFromMaster_ShouldReturnUser_WhenUserExists() {
        when(userMapper.selectById(1001L)).thenReturn(testUser);
        
        User result = userService.getFromMaster(1001L);
        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(25, result.getAge());
        verify(userMapper, times(1)).selectById(1001L);
    }

    @Test
    void getAllUsers_ShouldReturnUserList() {
        List<User> users = Arrays.asList(testUser);
        when(userMapper.selectList(null)).thenReturn(users);
        
        List<User> result = userService.getAllUsers();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        verify(userMapper, times(1)).selectList(null);
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() {
        User updatedUser = new User("updated", "updated@test.com", 30);
        updatedUser.setId(1001L);
        
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        
        User result = userService.updateUser(updatedUser);
        
        assertNotNull(result);
        assertEquals("updated", result.getUsername());
        assertEquals("updated@test.com", result.getEmail());
        assertEquals(30, result.getAge());
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void searchUsers_ShouldReturnMatchingUsers() {
        List<User> users = Arrays.asList(testUser);
        when(userMapper.selectList(any())).thenReturn(users);
        
        List<User> result = userService.searchUsers("test");
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        verify(userMapper, times(1)).selectList(any());
    }
}