package com.fomo.backend.controller;

import com.fomo.backend.service.BlockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BlockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlockService blockService;

    @Test
    @WithMockUser(username = "test@example.com")
    void blockUser_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(blockService).blockUser(anyString(), any());

        mockMvc.perform(post("/api/v1/blocks/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User blocked"));
    }

    @Test
    void blockUser_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/blocks/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void unblockUser_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(blockService).unblockUser(anyString(), any());

        mockMvc.perform(delete("/api/v1/blocks/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User unblocked"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getBlockedUsers_returns200() throws Exception {
        when(blockService.getBlockedUsers(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/blocks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
