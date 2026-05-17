package com.fomo.backend.controller;

import com.fomo.backend.dto.response.ApiResponse;
import com.fomo.backend.dto.response.StoryResponse;
import com.fomo.backend.service.StoryService;
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
@RequestMapping("/api/v1/stories")
@RequiredArgsConstructor
@Tag(name = "Stories", description = "Story management - stories expire after 24 hours")
public class StoryController {

    private final StoryService storyService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a new story (text and/or image/video, expires in 24h)")
    public ResponseEntity<StoryResponse> createStory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String content,
            @RequestParam(value = "media", required = false) MultipartFile media) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(storyService.createStory(userDetails.getUsername(), content, media));
    }

    @GetMapping
    @Operation(summary = "Get active stories from you and your friends (24h)")
    public ResponseEntity<List<StoryResponse>> getFriendsStories(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(storyService.getFriendsStories(userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a story")
    public ResponseEntity<ApiResponse> deleteStory(@AuthenticationPrincipal UserDetails userDetails,
                                                    @PathVariable UUID id) {
        storyService.deleteStory(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.ok("Story deleted"));
    }
}
