package com.demo.readwrite.controller;

import com.demo.readwrite.entity.User;
import com.demo.readwrite.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        User user = new User("testuser", "test@example.com", "13800000001", "password123");
        user.setId(1001L);
        
        when(userService.createUser(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(user);

        mockMvc.perform(post("/users")
                .param("username", "testuser")
                .param("email", "test@example.com")
                .param("phone", "13800000001")
                .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1001))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).createUser("testuser", "test@example.com", "13800000001", "password123");
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() throws Exception {
        User user = new User("testuser", "test@example.com", "13800000001", "password123");
        user.setId(1001L);
        
        when(userService.getUserById(1001L)).thenReturn(user);

        mockMvc.perform(get("/users/1001"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.id").value(1001))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService, times(1)).getUserById(1001L);
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        when(userService.getUserById(9999L)).thenReturn(null);

        mockMvc.perform(get("/users/9999"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(9999L);
    }

    @Test
    void getAllUsers_ShouldReturnUserList() throws Exception {
        User user1 = new User("user1", "user1@example.com", "13800000001", "password123");
        User user2 = new User("user2", "user2@example.com", "13800000002", "password123");
        List<User> users = Arrays.asList(user1, user2);
        
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void authenticate_ShouldReturnUser_WhenCredentialsValid() throws Exception {
        User user = new User("testuser", "test@example.com", "13800000001", "password123");
        user.setId(1001L);
        
        when(userService.authenticate("testuser", "password123")).thenReturn(user);

        mockMvc.perform(post("/users/authenticate")
                .param("username", "testuser")
                .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1001))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService, times(1)).authenticate("testuser", "password123");
    }

    @Test
    void authenticate_ShouldReturnNotFound_WhenCredentialsInvalid() throws Exception {
        when(userService.authenticate("testuser", "wrongpassword")).thenReturn(null);

        mockMvc.perform(post("/users/authenticate")
                .param("username", "testuser")
                .param("password", "wrongpassword"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).authenticate("testuser", "wrongpassword");
    }

    @Test
    void deleteUser_ShouldReturnTrue_WhenUserDeleted() throws Exception {
        when(userService.deleteUser(1001L)).thenReturn(true);

        mockMvc.perform(delete("/users/1001"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userService, times(1)).deleteUser(1001L);
    }
}