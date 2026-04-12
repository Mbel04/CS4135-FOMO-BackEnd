package com.fomo.backend.controller;

import com.fomo.backend.dto.request.NotificationSettingsRequest;
import com.fomo.backend.dto.response.ApiResponse;
import com.fomo.backend.dto.response.NotificationResponse;
import com.fomo.backend.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get all notifications for the current user")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(notificationService.getNotifications(userDetails.getUsername()));
    }

    @PostMapping("/{notificationId}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse> markAsRead(@AuthenticationPrincipal UserDetails userDetails,
                                                   @PathVariable UUID notificationId) {
        notificationService.markAsRead(userDetails.getUsername(), notificationId);
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read"));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read"));
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete a notification")
    public ResponseEntity<ApiResponse> deleteNotification(@AuthenticationPrincipal UserDetails userDetails,
                                                           @PathVariable UUID notificationId) {
        notificationService.deleteNotification(userDetails.getUsername(), notificationId);
        return ResponseEntity.ok(ApiResponse.ok("Notification deleted"));
    }

    @PutMapping("/settings")
    @Operation(summary = "Update notification preferences")
    public ResponseEntity<ApiResponse> updateSettings(@AuthenticationPrincipal UserDetails userDetails,
                                                       @RequestBody NotificationSettingsRequest request) {
        notificationService.updateSettings(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Notification settings updated"));
    }
}
