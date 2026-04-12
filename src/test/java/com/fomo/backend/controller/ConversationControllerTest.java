package com.fomo.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fomo.backend.dto.request.SendMessageRequest;
import com.fomo.backend.dto.response.ConversationResponse;
import com.fomo.backend.dto.response.MessageResponse;
import com.fomo.backend.dto.response.UserResponse;
import com.fomo.backend.service.ConversationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConversationService conversationService;

    private ConversationResponse buildConversation() {
        ConversationResponse c = new ConversationResponse();
        c.setId(UUID.randomUUID());
        c.setParticipants(Set.of());
        c.setCreatedAt(LocalDateTime.now());
        return c;
    }

    private MessageResponse buildMessage() {
        MessageResponse m = new MessageResponse();
        m.setId(UUID.randomUUID());
        m.setConversationId(UUID.randomUUID());
        m.setContent("Hello!");
        m.setCreatedAt(LocalDateTime.now());
        UserResponse sender = new UserResponse();
        sender.setUsername("testuser");
        m.setSender(sender);
        return m;
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getOrCreateDirect_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        when(conversationService.getOrCreateDirectConversation(anyString(), any())).thenReturn(buildConversation());

        mockMvc.perform(post("/api/v1/conversations/direct/" + userId))
                .andExpect(status().isOk());
    }

    @Test
    void getOrCreateDirect_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/conversations/direct/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getConversations_returns200() throws Exception {
        when(conversationService.getConversations(anyString())).thenReturn(List.of(buildConversation()));

        mockMvc.perform(get("/api/v1/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getMessages_returns200() throws Exception {
        UUID convId = UUID.randomUUID();
        when(conversationService.getMessages(anyString(), any())).thenReturn(List.of(buildMessage()));

        mockMvc.perform(get("/api/v1/conversations/" + convId + "/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Hello!"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void sendMessage_returns200() throws Exception {
        UUID convId = UUID.randomUUID();
        SendMessageRequest request = new SendMessageRequest();
        request.setMessageContent("Hello!");

        when(conversationService.sendMessage(anyString(), any(), any())).thenReturn(buildMessage());

        mockMvc.perform(post("/api/v1/conversations/" + convId + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello!"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void sendMessage_missingContent_returns400() throws Exception {
        UUID convId = UUID.randomUUID();
        SendMessageRequest request = new SendMessageRequest();

        mockMvc.perform(post("/api/v1/conversations/" + convId + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void markAsRead_returns200() throws Exception {
        UUID convId = UUID.randomUUID();
        doNothing().when(conversationService).markConversationAsRead(anyString(), any());

        mockMvc.perform(post("/api/v1/conversations/" + convId + "/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Messages marked as read"));
    }
}
