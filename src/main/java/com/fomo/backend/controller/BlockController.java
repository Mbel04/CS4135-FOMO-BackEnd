package com.fomo.backend.controller;

import com.fomo.backend.dto.response.ApiResponse;
import com.fomo.backend.dto.response.UserResponse;
import com.fomo.backend.service.BlockService;
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
@RequestMapping("/api/v1/blocks")
@RequiredArgsConstructor
@Tag(name = "Blocks", description = "User blocking management")
public class BlockController {

    private final BlockService blockService;

    @PostMapping("/{userId}")
    @Operation(summary = "Block a user")
    public ResponseEntity<ApiResponse> blockUser(@AuthenticationPrincipal UserDetails userDetails,
                                                  @PathVariable UUID userId) {
        blockService.blockUser(userDetails.getUsername(), userId);
        return ResponseEntity.ok(ApiResponse.ok("User blocked"));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Unblock a user")
    public ResponseEntity<ApiResponse> unblockUser(@AuthenticationPrincipal UserDetails userDetails,
                                                    @PathVariable UUID userId) {
        blockService.unblockUser(userDetails.getUsername(), userId);
        return ResponseEntity.ok(ApiResponse.ok("User unblocked"));
    }

    @GetMapping
    @Operation(summary = "Get all blocked users")
    public ResponseEntity<List<UserResponse>> getBlockedUsers(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(blockService.getBlockedUsers(userDetails.getUsername()));
    }
}
