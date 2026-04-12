package com.fomo.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fomo.backend.dto.request.NotificationSettingsRequest;
import com.fomo.backend.dto.response.NotificationResponse;
import com.fomo.backend.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    private NotificationResponse buildNotification() {
        NotificationResponse n = new NotificationResponse();
        n.setId(UUID.randomUUID());
        n.setType("LIKE");
        n.setMessage("Someone liked your post");
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.now());
        return n;
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getNotifications_returns200() throws Exception {
        when(notificationService.getNotifications(anyString())).thenReturn(List.of(buildNotification()));

        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("LIKE"));
    }

    @Test
    void getNotifications_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void markAsRead_returns200() throws Exception {
        UUID notificationId = UUID.randomUUID();
        doNothing().when(notificationService).markAsRead(anyString(), any());

        mockMvc.perform(post("/api/v1/notifications/" + notificationId + "/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notification marked as read"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void markAllAsRead_returns200() throws Exception {
        doNothing().when(notificationService).markAllAsRead(anyString());

        mockMvc.perform(post("/api/v1/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All notifications marked as read"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteNotification_returns200() throws Exception {
        UUID notificationId = UUID.randomUUID();
        doNothing().when(notificationService).deleteNotification(anyString(), any());

        mockMvc.perform(delete("/api/v1/notifications/" + notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notification deleted"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateSettings_returns200() throws Exception {
        NotificationSettingsRequest request = new NotificationSettingsRequest();
        request.setLikes(true);
        request.setFriendRequests(true);
        request.setTags(false);
        request.setMessages(true);
        request.setStories(false);

        doNothing().when(notificationService).updateSettings(anyString(), any());

        mockMvc.perform(put("/api/v1/notifications/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notification settings updated"));
    }
}
