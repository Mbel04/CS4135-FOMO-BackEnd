package com.fomo.backend.controller;

import com.fomo.backend.dto.response.ApiResponse;
import com.fomo.backend.dto.response.UserResponse;
import com.fomo.backend.service.CloseFriendService;
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
@RequestMapping("/api/v1/close-friends")
@RequiredArgsConstructor
@Tag(name = "Close Friends", description = "Close friend list management")
public class CloseFriendController {

    private final CloseFriendService closeFriendService;

    @PostMapping("/{userId}")
    @Operation(summary = "Add a friend as a close friend")
    public ResponseEntity<ApiResponse> addCloseFriend(@AuthenticationPrincipal UserDetails userDetails,
                                                       @PathVariable UUID userId) {
        closeFriendService.addCloseFriend(userDetails.getUsername(), userId);
        return ResponseEntity.ok(ApiResponse.ok("Added as close friend"));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Remove a close friend")
    public ResponseEntity<ApiResponse> removeCloseFriend(@AuthenticationPrincipal UserDetails userDetails,
                                                          @PathVariable UUID userId) {
        closeFriendService.removeCloseFriend(userDetails.getUsername(), userId);
        return ResponseEntity.ok(ApiResponse.ok("Removed from close friends"));
    }

    @GetMapping
    @Operation(summary = "Get all close friends")
    public ResponseEntity<List<UserResponse>> getCloseFriends(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(closeFriendService.getCloseFriends(userDetails.getUsername()));
    }
}
