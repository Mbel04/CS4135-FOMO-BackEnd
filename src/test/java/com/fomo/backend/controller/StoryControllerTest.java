package com.fomo.backend.controller;

import com.fomo.backend.dto.response.StoryResponse;
import com.fomo.backend.dto.response.UserResponse;
import com.fomo.backend.service.StoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
class StoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StoryService storyService;

    private StoryResponse buildStory() {
        StoryResponse s = new StoryResponse();
        s.setId(UUID.randomUUID());
        s.setContent("My story");
        s.setCreatedAt(LocalDateTime.now());
        s.setExpiresAt(LocalDateTime.now().plusHours(24));
        UserResponse user = new UserResponse();
        user.setUsername("testuser");
        s.setUser(user);
        return s;
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createStory_withText_returns201() throws Exception {
        when(storyService.createStory(anyString(), any(), any())).thenReturn(buildStory());

        mockMvc.perform(multipart("/api/v1/stories")
                        .param("content", "My story"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("My story"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createStory_withMedia_returns201() throws Exception {
        when(storyService.createStory(anyString(), any(), any())).thenReturn(buildStory());

        MockMultipartFile media = new MockMultipartFile("media", "story.mp4",
                "video/mp4", "fake-video-bytes".getBytes());

        mockMvc.perform(multipart("/api/v1/stories")
                        .file(media))
                .andExpect(status().isCreated());
    }

    @Test
    void createStory_noAuth_returns401() throws Exception {
        mockMvc.perform(multipart("/api/v1/stories")
                        .param("content", "My story"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getFriendsStories_returns200() throws Exception {
        when(storyService.getFriendsStories(anyString())).thenReturn(List.of(buildStory()));

        mockMvc.perform(get("/api/v1/stories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("My story"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteStory_returns200() throws Exception {
        UUID storyId = UUID.randomUUID();
        doNothing().when(storyService).deleteStory(anyString(), any());

        mockMvc.perform(delete("/api/v1/stories/" + storyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Story deleted"));
    }
}
