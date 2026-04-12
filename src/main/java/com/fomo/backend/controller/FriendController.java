package com.fomo.backend.controller;

import com.fomo.backend.dto.response.ApiResponse;
import com.fomo.backend.dto.response.FriendRequestResponse;
import com.fomo.backend.dto.response.UserResponse;
import com.fomo.backend.service.FriendService;
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
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
@Tag(name = "Friends", description = "Friend request and friendship management")
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/requests/{userId}")
    @Operation(summary = "Send a friend request to a user")
    public ResponseEntity<ApiResponse> sendFriendRequest(@AuthenticationPrincipal UserDetails userDetails,
                                                          @PathVariable UUID userId) {
        friendService.sendFriendRequest(userDetails.getUsername(), userId);
        return ResponseEntity.ok(ApiResponse.ok("Friend request sent"));
    }

    @GetMapping("/requests")
    @Operation(summary = "Get all incoming friend requests")
    public ResponseEntity<List<FriendRequestResponse>> getIncomingRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(friendService.getIncomingRequests(userDetails.getUsername()));
    }

    @PostMapping("/requests/{requestId}/accept")
    @Operation(summary = "Accept a friend request")
    public ResponseEntity<ApiResponse> acceptRequest(@AuthenticationPrincipal UserDetails userDetails,
                                                      @PathVariable UUID requestId) {
        friendService.acceptRequest(userDetails.getUsername(), requestId);
        return ResponseEntity.ok(ApiResponse.ok("Friend request accepted"));
    }

    @PostMapping("/requests/{requestId}/decline")
    @Operation(summary = "Decline a friend request")
    public ResponseEntity<ApiResponse> declineRequest(@AuthenticationPrincipal UserDetails userDetails,
                                                       @PathVariable UUID requestId) {
        friendService.declineRequest(userDetails.getUsername(), requestId);
        return ResponseEntity.ok(ApiResponse.ok("Friend request declined"));
    }

    @DeleteMapping("/{friendId}")
    @Operation(summary = "Remove a friend")
    public ResponseEntity<ApiResponse> removeFriend(@AuthenticationPrincipal UserDetails userDetails,
                                                     @PathVariable UUID friendId) {
        friendService.removeFriend(userDetails.getUsername(), friendId);
        return ResponseEntity.ok(ApiResponse.ok("Friend removed"));
    }

    @GetMapping
    @Operation(summary = "Get all friends")
    public ResponseEntity<List<UserResponse>> getFriends(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(friendService.getFriends(userDetails.getUsername()));
    }
}
