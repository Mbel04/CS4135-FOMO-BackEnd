package com.fomo.backend.controller;

import com.fomo.backend.dto.request.CreateReportRequest;
import com.fomo.backend.dto.response.ApiResponse;
import com.fomo.backend.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Report users or posts")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(summary = "Create a report for a user or post")
    public ResponseEntity<ApiResponse> createReport(@AuthenticationPrincipal UserDetails userDetails,
                                                     @Valid @RequestBody CreateReportRequest request) {
        reportService.createReport(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Report submitted"));
    }
}
