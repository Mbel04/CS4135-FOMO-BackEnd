package com.fomo.backend.controller;

import com.fomo.backend.dto.response.PostResponse;
import com.fomo.backend.dto.response.UserResponse;
import com.fomo.backend.service.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchService searchService;

    @Test
    @WithMockUser(username = "test@example.com")
    void searchUsers_returns200() throws Exception {
        UserResponse user = new UserResponse();
        user.setId(UUID.randomUUID());
        user.setUsername("john");
        when(searchService.searchUsers(anyString())).thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/search/users?q=john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("john"));
    }

    @Test
    void searchUsers_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/search/users?q=john"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void searchPosts_returns200() throws Exception {
        when(searchService.searchPostsByCategory(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/search/posts?q=tech"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
