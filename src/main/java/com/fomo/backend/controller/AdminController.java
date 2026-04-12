package com.fomo.backend.controller;

import com.fomo.backend.dto.response.ApiResponse;
import com.fomo.backend.entity.Report;
import com.fomo.backend.service.AdminService;
import com.fomo.backend.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin-only endpoints for moderation")
public class AdminController {

    private final AdminService adminService;
    private final ReportService reportService;

    @GetMapping("/reports")
    @Operation(summary = "Get all unresolved reports")
    public ResponseEntity<List<Report>> getReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @PostMapping("/reports/{id}/resolve")
    @Operation(summary = "Mark a report as resolved")
    public ResponseEntity<ApiResponse> resolveReport(@PathVariable UUID id) {
        reportService.resolveReport(id);
        return ResponseEntity.ok(ApiResponse.ok("Report resolved"));
    }

    @PostMapping("/users/{userId}/verify")
    @Operation(summary = "Verify a user account")
    public ResponseEntity<ApiResponse> verifyUser(@PathVariable UUID userId) {
        adminService.verifyUser(userId);
        return ResponseEntity.ok(ApiResponse.ok("User verified"));
    }

    @PostMapping("/users/{userId}/unverify")
    @Operation(summary = "Remove user verification")
    public ResponseEntity<ApiResponse> unverifyUser(@PathVariable UUID userId) {
        adminService.unverifyUser(userId);
        return ResponseEntity.ok(ApiResponse.ok("User unverified"));
    }

    @PostMapping("/users/{userId}/ban")
    @Operation(summary = "Ban a user")
    public ResponseEntity<ApiResponse> banUser(@PathVariable UUID userId) {
        adminService.banUser(userId);
        return ResponseEntity.ok(ApiResponse.ok("User banned"));
    }

    @PostMapping("/users/{userId}/unban")
    @Operation(summary = "Unban a user")
    public ResponseEntity<ApiResponse> unbanUser(@PathVariable UUID userId) {
        adminService.unbanUser(userId);
        return ResponseEntity.ok(ApiResponse.ok("User unbanned"));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete a user account")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable UUID userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.ok("User deleted"));
    }
}
