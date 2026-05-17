package com.fomo.backend.controller;

import com.fomo.backend.dto.response.ApiResponse;
import com.fomo.backend.dto.response.PostResponse;
import com.fomo.backend.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Post management, likes, and saves")
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a new post (text and/or image/video)")
    public ResponseEntity<PostResponse> createPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) List<UUID> categoryIds,
            @RequestParam(required = false) List<String> taggedUserIds,
            @RequestParam(value = "media", required = false) MultipartFile media) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createPost(userDetails.getUsername(), content, categoryIds, taggedUserIds, media));
    }

    @GetMapping
    @Operation(summary = "Get post feed")
    public ResponseEntity<List<PostResponse>> getFeed() {
        return ResponseEntity.ok(postService.getFeed());
    }

    @GetMapping("/{postId}")
    @Operation(summary = "Get a single post by ID")
    public ResponseEntity<PostResponse> getPost(@PathVariable UUID postId) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "Delete a post (owner or admin)")
    public ResponseEntity<ApiResponse> deletePost(@AuthenticationPrincipal UserDetails userDetails,
                                                   @PathVariable UUID postId) {
        postService.deletePost(userDetails.getUsername(), postId);
        return ResponseEntity.ok(ApiResponse.ok("Post deleted"));
    }

    @PostMapping("/{postId}/like")
    @Operation(summary = "Like a post")
    public ResponseEntity<ApiResponse> likePost(@AuthenticationPrincipal UserDetails userDetails,
                                                 @PathVariable UUID postId) {
        postService.likePost(userDetails.getUsername(), postId);
        return ResponseEntity.ok(ApiResponse.ok("Post liked"));
    }

    @DeleteMapping("/{postId}/like")
    @Operation(summary = "Unlike a post")
    public ResponseEntity<ApiResponse> unlikePost(@AuthenticationPrincipal UserDetails userDetails,
                                                   @PathVariable UUID postId) {
        postService.unlikePost(userDetails.getUsername(), postId);
        return ResponseEntity.ok(ApiResponse.ok("Post unliked"));
    }

    @PostMapping("/{postId}/save")
    @Operation(summary = "Save a post")
    public ResponseEntity<ApiResponse> savePost(@AuthenticationPrincipal UserDetails userDetails,
                                                 @PathVariable UUID postId) {
        postService.savePost(userDetails.getUsername(), postId);
        return ResponseEntity.ok(ApiResponse.ok("Post saved"));
    }

    @DeleteMapping("/{postId}/save")
    @Operation(summary = "Remove a saved post")
    public ResponseEntity<ApiResponse> unsavePost(@AuthenticationPrincipal UserDetails userDetails,
                                                   @PathVariable UUID postId) {
        postService.unsavePost(userDetails.getUsername(), postId);
        return ResponseEntity.ok(ApiResponse.ok("Post removed from saves"));
    }
}
