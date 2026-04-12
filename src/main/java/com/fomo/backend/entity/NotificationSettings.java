package com.fomo.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "notification_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private boolean likes = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean friendRequests = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean tags = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean messages = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean stories = true;
}
