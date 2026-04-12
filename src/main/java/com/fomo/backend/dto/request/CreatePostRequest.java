package com.fomo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreatePostRequest {

    @NotBlank(message = "Content is required")
    private String content;

    private List<UUID> categoryIds;
    private List<UUID> taggedUserIds;
}
