package com.fomo.backend.dto.response;

import com.fomo.backend.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class PostResponse {
    private UUID id;
    private UserResponse author;
    private String content;
    private String mediaUrl;
    private String mediaType;
    private Set<CategoryResponse> categories;
    private Set<UserResponse> taggedUsers;
    private long likeCount;
    private LocalDateTime createdAt;

    public static PostResponse from(Post post, long likeCount) {
        PostResponse r = new PostResponse();
        r.setId(post.getId());
        r.setAuthor(UserResponse.from(post.getAuthor()));
        r.setContent(post.getContent());
        r.setMediaUrl(post.getMediaUrl());
        r.setMediaType(post.getMediaType());
        r.setCategories(post.getCategories().stream().map(CategoryResponse::from).collect(Collectors.toSet()));
        r.setTaggedUsers(post.getTaggedUsers().stream().map(UserResponse::from).collect(Collectors.toSet()));
        r.setLikeCount(likeCount);
        r.setCreatedAt(post.getCreatedAt());
        return r;
    }
}
