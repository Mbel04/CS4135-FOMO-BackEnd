package com.fomo.backend.dto.response;

import com.fomo.backend.entity.FriendRequest;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FriendRequestResponse {
    private UUID id;
    private UserResponse sender;
    private UserResponse receiver;
    private String status;
    private LocalDateTime createdAt;

    public static FriendRequestResponse from(FriendRequest fr) {
        FriendRequestResponse r = new FriendRequestResponse();
        r.setId(fr.getId());
        r.setSender(UserResponse.from(fr.getSender()));
        r.setReceiver(UserResponse.from(fr.getReceiver()));
        r.setStatus(fr.getStatus().name());
        r.setCreatedAt(fr.getCreatedAt());
        return r;
    }
}
