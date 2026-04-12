package com.fomo.backend.controller;

import com.fomo.backend.dto.response.FriendRequestResponse;
import com.fomo.backend.dto.response.UserResponse;
import com.fomo.backend.service.FriendService;
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
class FriendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FriendService friendService;

    @Test
    @WithMockUser(username = "test@example.com")
    void sendFriendRequest_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(friendService).sendFriendRequest(anyString(), any());

        mockMvc.perform(post("/api/v1/friends/requests/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Friend request sent"));
    }

    @Test
    void sendFriendRequest_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/friends/requests/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getIncomingRequests_returns200() throws Exception {
        when(friendService.getIncomingRequests(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/friends/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void acceptRequest_returns200() throws Exception {
        UUID requestId = UUID.randomUUID();
        doNothing().when(friendService).acceptRequest(anyString(), any());

        mockMvc.perform(post("/api/v1/friends/requests/" + requestId + "/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Friend request accepted"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void declineRequest_returns200() throws Exception {
        UUID requestId = UUID.randomUUID();
        doNothing().when(friendService).declineRequest(anyString(), any());

        mockMvc.perform(post("/api/v1/friends/requests/" + requestId + "/decline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Friend request declined"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void removeFriend_returns200() throws Exception {
        UUID friendId = UUID.randomUUID();
        doNothing().when(friendService).removeFriend(anyString(), any());

        mockMvc.perform(delete("/api/v1/friends/" + friendId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Friend removed"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getFriends_returns200() throws Exception {
        when(friendService.getFriends(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
