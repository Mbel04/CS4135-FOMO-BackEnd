package com.fomo.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_post_id")
    private Post reportedPost;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    @Builder.Default
    private boolean resolved = false;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
