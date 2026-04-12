package com.fomo.backend.controller;

import com.fomo.backend.dto.request.SendMessageRequest;
import com.fomo.backend.dto.response.ApiResponse;
import com.fomo.backend.dto.response.ConversationResponse;
import com.fomo.backend.dto.response.MessageResponse;
import com.fomo.backend.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Conversations", description = "Direct messaging and conversations")
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping("/api/v1/conversations/direct/{userId}")
    @Operation(summary = "Create or get a direct conversation with a user")
    public ResponseEntity<ConversationResponse> getOrCreateDirect(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(conversationService.getOrCreateDirectConversation(userDetails.getUsername(), userId));
    }

    @GetMapping("/api/v1/conversations")
    @Operation(summary = "Get all conversations for the current user")
    public ResponseEntity<List<ConversationResponse>> getConversations(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(conversationService.getConversations(userDetails.getUsername()));
    }

    @GetMapping("/api/v1/conversations/{conversationId}/messages")
    @Operation(summary = "Get all messages in a conversation")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID conversationId) {
        return ResponseEntity.ok(conversationService.getMessages(userDetails.getUsername(), conversationId));
    }

    @PostMapping("/api/v1/conversations/{conversationId}/messages")
    @Operation(summary = "Send a message in a conversation")
    public ResponseEntity<MessageResponse> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(conversationService.sendMessage(userDetails.getUsername(), conversationId, request));
    }

    @GetMapping("/api/v1/messages/{userId}")
    @Operation(summary = "Get the message thread between the current user and another user")
    public ResponseEntity<List<MessageResponse>> getMessageThread(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(conversationService.getMessageThread(userDetails.getUsername(), userId));
    }

    @PostMapping("/api/v1/conversations/{conversationId}/read")
    @Operation(summary = "Mark all messages in a conversation as read")
    public ResponseEntity<ApiResponse> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID conversationId) {
        conversationService.markConversationAsRead(userDetails.getUsername(), conversationId);
        return ResponseEntity.ok(ApiResponse.ok("Messages marked as read"));
    }
}
