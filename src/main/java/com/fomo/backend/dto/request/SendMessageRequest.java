package com.fomo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class SendMessageRequest {

    @NotBlank(message = "Message content is required")
    private String messageContent;

    private UUID sharedPostId;
}
