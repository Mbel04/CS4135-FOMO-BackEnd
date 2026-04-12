package com.fomo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateGroupChatRequest {

    @NotBlank(message = "New name is required")
    private String newName;
}
