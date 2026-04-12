package com.fomo.backend.dto.response;

import com.fomo.backend.entity.Story;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class StoryResponse {
    private UUID id;
    private UserResponse user;
    private String content;
    private String mediaUrl;
    private String mediaType;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public static StoryResponse from(Story story) {
        StoryResponse r = new StoryResponse();
        r.setId(story.getId());
        r.setUser(UserResponse.from(story.getUser()));
        r.setContent(story.getContent());
        r.setMediaUrl(story.getMediaUrl());
        r.setMediaType(story.getMediaType());
        r.setCreatedAt(story.getCreatedAt());
        r.setExpiresAt(story.getExpiresAt());
        return r;
    }
}
