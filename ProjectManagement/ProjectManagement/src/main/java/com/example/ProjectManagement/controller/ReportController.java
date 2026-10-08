package com.example.ProjectManagement.controller;

import com.example.ProjectManagement.DTO.Response.ApiResponse;
import com.example.ProjectManagement.DTO.Response.ProjectProgressReportResponse;
import com.example.ProjectManagement.DTO.Response.UserPerformanceReportResponse;
import com.example.ProjectManagement.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/project-progress")
    public ApiResponse<ProjectProgressReportResponse> getProjectProgressReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Authentication authentication) {
        String username = authentication.getName();
        ProjectProgressReportResponse report = reportService.generateProjectProgressReport(from, to, username);
        return ApiResponse.success("Project progress report generated successfully", report);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/user-performance")
    public ApiResponse<UserPerformanceReportResponse> getUserPerformanceReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        UserPerformanceReportResponse report = reportService.generateUserPerformanceReport(from, to);
        return ApiResponse.success("User performance report generated successfully", report);
    }
}
