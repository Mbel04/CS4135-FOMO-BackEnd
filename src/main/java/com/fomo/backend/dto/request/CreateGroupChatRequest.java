package com.fomo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateGroupChatRequest {

    @NotBlank(message = "Group chat name is required")
    private String name;

    /** Each entry: user id (UUID string) or username (case-insensitive). */
    @NotEmpty(message = "At least one member is required")
    private List<String> memberIds;

    private boolean initialMessage;
    private String messageContent;
}
