package com.fomo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateGroupChatRequest {

    @NotBlank(message = "Group chat name is required")
    private String name;

    @NotEmpty(message = "At least one member is required")
    private List<UUID> memberIds;

    private boolean initialMessage;
    private String messageContent;
}
