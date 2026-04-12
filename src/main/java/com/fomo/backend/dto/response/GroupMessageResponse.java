package com.fomo.backend.dto.response;

import com.fomo.backend.entity.GroupMessage;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class GroupMessageResponse {
    private UUID id;
    private UUID groupChatId;
    private UserResponse sender;
    private String content;
    private UUID sharedPostId;
    private LocalDateTime createdAt;

    public static GroupMessageResponse from(GroupMessage message) {
        GroupMessageResponse r = new GroupMessageResponse();
        r.setId(message.getId());
        r.setGroupChatId(message.getGroupChat().getId());
        r.setSender(UserResponse.from(message.getSender()));
        r.setContent(message.getContent());
        r.setSharedPostId(message.getSharedPostId());
        r.setCreatedAt(message.getCreatedAt());
        return r;
    }
}
