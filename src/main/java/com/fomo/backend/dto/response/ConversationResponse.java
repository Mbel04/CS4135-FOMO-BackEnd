package com.fomo.backend.dto.response;

import com.fomo.backend.entity.Conversation;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class ConversationResponse {
    private UUID id;
    private Set<UserResponse> participants;
    private LocalDateTime createdAt;

    public static ConversationResponse from(Conversation conversation) {
        ConversationResponse r = new ConversationResponse();
        r.setId(conversation.getId());
        r.setParticipants(conversation.getParticipants().stream()
                .map(UserResponse::from).collect(Collectors.toSet()));
        r.setCreatedAt(conversation.getCreatedAt());
        return r;
    }
}
