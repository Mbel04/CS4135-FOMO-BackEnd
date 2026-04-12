package com.fomo.backend.service;

import com.fomo.backend.dto.request.CreateReportRequest;
import com.fomo.backend.entity.Post;
import com.fomo.backend.entity.Report;
import com.fomo.backend.entity.User;
import com.fomo.backend.exception.BadRequestException;
import com.fomo.backend.exception.ResourceNotFoundException;
import com.fomo.backend.repository.PostRepository;
import com.fomo.backend.repository.ReportRepository;
import com.fomo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    public void createReport(String email, CreateReportRequest request) {
        User reporter = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getReportedUserId() == null && request.getReportedPostId() == null) {
            throw new BadRequestException("Must report a user or a post");
        }

        User reportedUser = null;
        if (request.getReportedUserId() != null) {
            reportedUser = userRepository.findById(request.getReportedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reported user not found"));
        }

        Post reportedPost = null;
        if (request.getReportedPostId() != null) {
            reportedPost = postRepository.findById(request.getReportedPostId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reported post not found"));
        }

        Report report = Report.builder()
                .reporter(reporter)
                .reportedUser(reportedUser)
                .reportedPost(reportedPost)
                .reason(request.getReason())
                .build();
        reportRepository.save(report);
    }

    public List<Report> getAllReports() {
        return reportRepository.findByResolvedFalse();
    }

    @Transactional
    public void resolveReport(java.util.UUID reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        report.setResolved(true);
        reportRepository.save(report);
    }
}
