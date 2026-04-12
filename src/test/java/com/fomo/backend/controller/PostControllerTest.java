package com.fomo.backend.controller;

import com.fomo.backend.dto.response.PostResponse;
import com.fomo.backend.dto.response.UserResponse;
import com.fomo.backend.service.PostService;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    private PostResponse buildPostResponse() {
        PostResponse post = new PostResponse();
        post.setId(UUID.randomUUID());
        post.setContent("Test post content");
        post.setLikeCount(0);
        post.setCreatedAt(LocalDateTime.now());
        post.setCategories(Collections.emptySet());
        post.setTaggedUsers(Collections.emptySet());
        UserResponse author = new UserResponse();
        author.setId(UUID.randomUUID());
        author.setUsername("testuser");
        post.setAuthor(author);
        return post;
    }

    @Test
    void getFeed_noAuth_returns200() throws Exception {
        when(postService.getFeed()).thenReturn(List.of(buildPostResponse()));
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Test post content"));
    }

    @Test
    void getPost_noAuth_returns200() throws Exception {
        UUID postId = UUID.randomUUID();
        when(postService.getPost(postId)).thenReturn(buildPostResponse());
        mockMvc.perform(get("/api/v1/posts/" + postId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createPost_withTextOnly_returns201() throws Exception {
        when(postService.createPost(anyString(), any(), any(), any(), any())).thenReturn(buildPostResponse());

        mockMvc.perform(multipart("/api/v1/posts")
                        .param("content", "Hello World"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createPost_withMedia_returns201() throws Exception {
        when(postService.createPost(anyString(), any(), any(), any(), any())).thenReturn(buildPostResponse());

        MockMultipartFile media = new MockMultipartFile("media", "photo.jpg",
                MediaType.IMAGE_JPEG_VALUE, "fake-image-bytes".getBytes());

        mockMvc.perform(multipart("/api/v1/posts")
                        .file(media)
                        .param("content", "Photo post"))
                .andExpect(status().isCreated());
    }

    @Test
    void createPost_noAuth_returns401() throws Exception {
        mockMvc.perform(multipart("/api/v1/posts")
                        .param("content", "Hello World"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deletePost_authenticated_returns200() throws Exception {
        UUID postId = UUID.randomUUID();
        doNothing().when(postService).deletePost(anyString(), any());

        mockMvc.perform(delete("/api/v1/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void likePost_authenticated_returns200() throws Exception {
        UUID postId = UUID.randomUUID();
        doNothing().when(postService).likePost(anyString(), any());

        mockMvc.perform(post("/api/v1/posts/" + postId + "/like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post liked"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void unlikePost_authenticated_returns200() throws Exception {
        UUID postId = UUID.randomUUID();
        doNothing().when(postService).unlikePost(anyString(), any());

        mockMvc.perform(delete("/api/v1/posts/" + postId + "/like"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void savePost_authenticated_returns200() throws Exception {
        UUID postId = UUID.randomUUID();
        doNothing().when(postService).savePost(anyString(), any());

        mockMvc.perform(post("/api/v1/posts/" + postId + "/save"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post saved"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void unsavePost_authenticated_returns200() throws Exception {
        UUID postId = UUID.randomUUID();
        doNothing().when(postService).unsavePost(anyString(), any());

        mockMvc.perform(delete("/api/v1/posts/" + postId + "/save"))
                .andExpect(status().isOk());
    }
}
