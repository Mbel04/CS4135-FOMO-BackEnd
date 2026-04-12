package com.fomo.backend.dto.response;

import com.fomo.backend.entity.Message;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MessageResponse {
    private UUID id;
    private UUID conversationId;
    private UserResponse sender;
    private String content;
    private UUID sharedPostId;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public static MessageResponse from(Message message) {
        MessageResponse r = new MessageResponse();
        r.setId(message.getId());
        r.setConversationId(message.getConversation().getId());
        r.setSender(UserResponse.from(message.getSender()));
        r.setContent(message.getContent());
        r.setSharedPostId(message.getSharedPostId());
        r.setCreatedAt(message.getCreatedAt());
        r.setReadAt(message.getReadAt());
        return r;
    }
}
