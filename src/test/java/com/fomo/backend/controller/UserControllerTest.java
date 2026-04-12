package com.fomo.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fomo.backend.dto.request.UpdateProfileRequest;
import com.fomo.backend.dto.response.PostResponse;
import com.fomo.backend.dto.response.UserResponse;
import com.fomo.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserResponse buildUser() {
        UserResponse user = new UserResponse();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setBio("Test bio");
        user.setRole("USER");
        return user;
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getMe_authenticated_returns200() throws Exception {
        when(userService.getMe(anyString())).thenReturn(buildUser());

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void getMe_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateMe_authenticated_returns200() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("newusername");
        request.setBio("New bio");

        UserResponse updated = buildUser();
        updated.setUsername("newusername");
        updated.setBio("New bio");
        when(userService.updateProfile(anyString(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newusername"))
                .andExpect(jsonPath("$.bio").value("New bio"));
    }

    @Test
    void updateMe_noAuth_returns401() throws Exception {
        mockMvc.perform(put("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getSavedPosts_authenticated_returns200() throws Exception {
        when(userService.getSavedPosts(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/users/me/saved-posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getSavedPosts_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/saved-posts"))
                .andExpect(status().isUnauthorized());
    }
}
