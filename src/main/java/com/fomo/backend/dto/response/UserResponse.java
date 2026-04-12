package com.fomo.backend.dto.response;

import com.fomo.backend.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String email;
    private String username;
    private String bio;
    private boolean verified;
    private boolean banned;
    private String role;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setEmail(user.getEmail());
        r.setUsername(user.getUsername());
        r.setBio(user.getBio());
        r.setVerified(user.isVerified());
        r.setBanned(user.isBanned());
        r.setRole(user.getRole().name());
        r.setCreatedAt(user.getCreatedAt());
        return r;
    }
}
