package com.fomo.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fomo.backend.dto.request.CreateGroupChatRequest;
import com.fomo.backend.dto.request.SendGroupMessageRequest;
import com.fomo.backend.dto.request.UpdateGroupChatRequest;
import com.fomo.backend.dto.response.GroupChatResponse;
import com.fomo.backend.dto.response.GroupMessageResponse;
import com.fomo.backend.dto.response.UserResponse;
import com.fomo.backend.service.GroupChatService;
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
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GroupChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GroupChatService groupChatService;

    private GroupChatResponse buildGroupChat() {
        GroupChatResponse g = new GroupChatResponse();
        g.setId(UUID.randomUUID());
        g.setName("Test Group");
        g.setMembers(Set.of());
        g.setCreatedAt(LocalDateTime.now());
        UserResponse creator = new UserResponse();
        creator.setUsername("testuser");
        g.setCreator(creator);
        return g;
    }

    private GroupMessageResponse buildGroupMessage() {
        GroupMessageResponse m = new GroupMessageResponse();
        m.setId(UUID.randomUUID());
        m.setGroupChatId(UUID.randomUUID());
        m.setContent("Hello group!");
        m.setCreatedAt(LocalDateTime.now());
        UserResponse sender = new UserResponse();
        sender.setUsername("testuser");
        m.setSender(sender);
        return m;
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createGroupChat_returns201() throws Exception {
        CreateGroupChatRequest request = new CreateGroupChatRequest();
        request.setName("Test Group");
        request.setMemberIds(List.of(UUID.randomUUID()));

        when(groupChatService.createGroupChat(anyString(), any())).thenReturn(buildGroupChat());

        mockMvc.perform(post("/api/v1/groupchats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Group"));
    }

    @Test
    void createGroupChat_noAuth_returns401() throws Exception {
        CreateGroupChatRequest request = new CreateGroupChatRequest();
        request.setName("Test Group");
        request.setMemberIds(List.of(UUID.randomUUID()));

        mockMvc.perform(post("/api/v1/groupchats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getGroupChats_returns200() throws Exception {
        when(groupChatService.getGroupChats(anyString())).thenReturn(List.of(buildGroupChat()));

        mockMvc.perform(get("/api/v1/groupchats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Group"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getGroupMessages_returns200() throws Exception {
        UUID groupId = UUID.randomUUID();
        when(groupChatService.getGroupMessages(anyString(), any())).thenReturn(List.of(buildGroupMessage()));

        mockMvc.perform(get("/api/v1/groupchats/" + groupId + "/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Hello group!"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void sendGroupMessage_returns200() throws Exception {
        UUID groupId = UUID.randomUUID();
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setMessageContent("Hello group!");

        when(groupChatService.sendGroupMessage(anyString(), any(), any())).thenReturn(buildGroupMessage());

        mockMvc.perform(post("/api/v1/groupchats/" + groupId + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello group!"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addMember_returns200() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(groupChatService.addMember(anyString(), any(), any())).thenReturn(buildGroupChat());

        mockMvc.perform(post("/api/v1/groupchats/" + groupId + "/members/add/" + userId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void removeMember_returns200() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        doNothing().when(groupChatService).removeMember(anyString(), any(), any());

        mockMvc.perform(delete("/api/v1/groupchats/" + groupId + "/members/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Member removed"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void leaveGroupChat_returns200() throws Exception {
        UUID groupId = UUID.randomUUID();
        doNothing().when(groupChatService).leaveGroupChat(anyString(), any());

        mockMvc.perform(post("/api/v1/groupchats/" + groupId + "/leave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Left the group chat"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateGroupChat_returns200() throws Exception {
        UUID groupId = UUID.randomUUID();
        UpdateGroupChatRequest request = new UpdateGroupChatRequest();
        request.setNewName("New Name");

        when(groupChatService.updateGroupChat(anyString(), any(), any())).thenReturn(buildGroupChat());

        mockMvc.perform(put("/api/v1/groupchats/" + groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
