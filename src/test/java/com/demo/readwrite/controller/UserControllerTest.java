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
        User user = new User("testuser", "test@example.com", 25);
        user.setId(1001L);
        
        when(userService.createUser(anyString(), anyString(), anyInt()))
                .thenReturn(user);

        mockMvc.perform(post("/users")
                .param("username", "testuser")
                .param("email", "test@example.com")
                .param("age", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1001))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.age").value(25));

        verify(userService, times(1)).createUser("testuser", "test@example.com", 25);
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() throws Exception {
        User user = new User("testuser", "test@example.com", 25);
        user.setId(1001L);
        
        when(userService.getById(1001L)).thenReturn(user);

        mockMvc.perform(get("/users/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1001))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.age").value(25));

        verify(userService, times(1)).getById(1001L);
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        when(userService.getById(9999L)).thenReturn(null);

        mockMvc.perform(get("/users/9999"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getById(9999L);
    }

    @Test
    void getAllUsers_ShouldReturnUserList() throws Exception {
        User user1 = new User("user1", "user1@example.com", 25);
        User user2 = new User("user2", "user2@example.com", 30);
        user1.setId(1L);
        user2.setId(2L);
        List<User> users = Arrays.asList(user1, user2);
        
        when(userService.list()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"));

        verify(userService, times(1)).list();
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        User user = new User("updateduser", "updated@example.com", 28);
        user.setId(1001L);
        
        when(userService.updateUser(any(User.class))).thenReturn(user);

        String userJson = objectMapper.writeValueAsString(user);

        mockMvc.perform(put("/users/1001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1001))
                .andExpect(jsonPath("$.username").value("updateduser"));

        verify(userService, times(1)).updateUser(any(User.class));
    }

    @Test
    void deleteUser_ShouldReturnTrue_WhenUserDeleted() throws Exception {
        when(userService.removeById(1001L)).thenReturn(true);

        mockMvc.perform(delete("/users/1001"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userService, times(1)).removeById(1001L);
    }

    @Test
    void getUserFromMaster_ShouldReturnUser() throws Exception {
        User user = new User("testuser", "test@example.com", 25);
        user.setId(1001L);
        
        when(userService.getFromMaster(1001L)).thenReturn(user);

        mockMvc.perform(get("/users/master-test/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1001))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService, times(1)).getFromMaster(1001L);
    }

    @Test
    void getUserByUsername_ShouldReturnUserList() throws Exception {
        User user = new User("testuser", "test@example.com", 25);
        user.setId(1001L);
        List<User> users = Arrays.asList(user);
        
        when(userService.findByUsername("testuser")).thenReturn(users);

        mockMvc.perform(get("/users/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].username").value("testuser"));

        verify(userService, times(1)).findByUsername("testuser");
    }
}