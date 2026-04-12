package com.fomo.backend.dto.request;

import lombok.Data;

@Data
public class NotificationSettingsRequest {
    private boolean likes;
    private boolean friendRequests;
    private boolean tags;
    private boolean messages;
    private boolean stories;
}
