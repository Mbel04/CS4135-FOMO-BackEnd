package com.fomo.backend.controller;

import com.fomo.backend.dto.request.CreateGroupChatRequest;
import com.fomo.backend.dto.request.SendGroupMessageRequest;
import com.fomo.backend.dto.request.UpdateGroupChatRequest;
import com.fomo.backend.dto.response.ApiResponse;
import com.fomo.backend.dto.response.GroupChatResponse;
import com.fomo.backend.dto.response.GroupMessageResponse;
import com.fomo.backend.service.GroupChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groupchats")
@RequiredArgsConstructor
@Tag(name = "Group Chats", description = "Group chat management and messaging")
public class GroupChatController {

    private final GroupChatService groupChatService;

    @PostMapping
    @Operation(summary = "Create a new group chat")
    public ResponseEntity<GroupChatResponse> createGroupChat(@AuthenticationPrincipal UserDetails userDetails,
                                                              @Valid @RequestBody CreateGroupChatRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupChatService.createGroupChat(userDetails.getUsername(), request));
    }

    @GetMapping
    @Operation(summary = "Get all group chats the user is in")
    public ResponseEntity<List<GroupChatResponse>> getGroupChats(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(groupChatService.getGroupChats(userDetails.getUsername()));
    }

    @GetMapping("/{groupId}/messages")
    @Operation(summary = "Get all messages in a group chat")
    public ResponseEntity<List<GroupMessageResponse>> getGroupMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(groupChatService.getGroupMessages(userDetails.getUsername(), groupId));
    }

    @PostMapping("/{groupId}/messages")
    @Operation(summary = "Send a message to a group chat")
    public ResponseEntity<GroupMessageResponse> sendGroupMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID groupId,
            @Valid @RequestBody SendGroupMessageRequest request) {
        return ResponseEntity.ok(groupChatService.sendGroupMessage(userDetails.getUsername(), groupId, request));
    }

    @PostMapping("/{groupId}/members/add/{userId}")
    @Operation(summary = "Add a member to a group chat")
    public ResponseEntity<GroupChatResponse> addMember(@AuthenticationPrincipal UserDetails userDetails,
                                                        @PathVariable UUID groupId,
                                                        @PathVariable UUID userId) {
        return ResponseEntity.ok(groupChatService.addMember(userDetails.getUsername(), groupId, userId));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    @Operation(summary = "Remove a member from a group chat")
    public ResponseEntity<ApiResponse> removeMember(@AuthenticationPrincipal UserDetails userDetails,
                                                     @PathVariable UUID groupId,
                                                     @PathVariable UUID userId) {
        groupChatService.removeMember(userDetails.getUsername(), groupId, userId);
        return ResponseEntity.ok(ApiResponse.ok("Member removed"));
    }

    @PostMapping("/{groupId}/leave")
    @Operation(summary = "Leave a group chat")
    public ResponseEntity<ApiResponse> leaveGroupChat(@AuthenticationPrincipal UserDetails userDetails,
                                                       @PathVariable UUID groupId) {
        groupChatService.leaveGroupChat(userDetails.getUsername(), groupId);
        return ResponseEntity.ok(ApiResponse.ok("Left the group chat"));
    }

    @PutMapping("/{groupId}")
    @Operation(summary = "Update a group chat name")
    public ResponseEntity<GroupChatResponse> updateGroupChat(@AuthenticationPrincipal UserDetails userDetails,
                                                              @PathVariable UUID groupId,
                                                              @Valid @RequestBody UpdateGroupChatRequest request) {
        return ResponseEntity.ok(groupChatService.updateGroupChat(userDetails.getUsername(), groupId, request));
    }
}
