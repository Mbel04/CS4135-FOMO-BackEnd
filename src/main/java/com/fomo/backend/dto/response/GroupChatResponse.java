package com.fomo.backend.dto.response;

import com.fomo.backend.entity.GroupChat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class GroupChatResponse {
    private UUID id;
    private String name;
    private UserResponse creator;
    private Set<UserResponse> members;
    private LocalDateTime createdAt;

    public static GroupChatResponse from(GroupChat groupChat) {
        GroupChatResponse r = new GroupChatResponse();
        r.setId(groupChat.getId());
        r.setName(groupChat.getName());
        r.setCreator(UserResponse.from(groupChat.getCreator()));
        r.setMembers(groupChat.getMembers().stream().map(UserResponse::from).collect(Collectors.toSet()));
        r.setCreatedAt(groupChat.getCreatedAt());
        return r;
    }
}
