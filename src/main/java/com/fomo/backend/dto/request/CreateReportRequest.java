package com.fomo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateReportRequest {

    private UUID reportedUserId;
    private UUID reportedPostId;

    @NotBlank(message = "Reason is required")
    private String reason;
}
