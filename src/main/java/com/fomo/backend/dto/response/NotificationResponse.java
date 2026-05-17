package com.fomo.backend.dto.response;

import com.fomo.backend.entity.Notification;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NotificationResponse {
    private UUID id;
    private String type;
    private String message;
    private boolean read;
    private UUID referenceId;
    /** How to route referenceId: post, conversation, group, user, story, none */
    private String referenceKind;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        NotificationResponse r = new NotificationResponse();
        r.setId(notification.getId());
        r.setType(notification.getType().name());
        r.setMessage(notification.getMessage());
        r.setRead(notification.isRead());
        r.setReferenceId(notification.getReferenceId());
        r.setCreatedAt(notification.getCreatedAt());
        return r;
    }
}
