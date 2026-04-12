package com.fomo.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fomo.backend.dto.request.CreateCategoryRequest;
import com.fomo.backend.dto.response.CategoryResponse;
import com.fomo.backend.dto.response.PostResponse;
import com.fomo.backend.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    private CategoryResponse buildCategory() {
        CategoryResponse c = new CategoryResponse();
        c.setId(UUID.randomUUID());
        c.setName("Technology");
        return c;
    }

    @Test
    void getCategories_noAuth_returns200() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(buildCategory()));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Technology"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void createCategory_asAdmin_returns201() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Technology");

        when(categoryService.createCategory(any())).thenReturn(buildCategory());

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Technology"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void createCategory_asUser_returns403() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Technology");

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPostsByCategory_noAuth_returns200() throws Exception {
        UUID categoryId = UUID.randomUUID();
        when(categoryService.getPostsByCategory(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/categories/" + categoryId + "/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
