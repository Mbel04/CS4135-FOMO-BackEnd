package com.fomo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateStoryRequest {

    @NotBlank(message = "Content is required")
    private String content;
}
