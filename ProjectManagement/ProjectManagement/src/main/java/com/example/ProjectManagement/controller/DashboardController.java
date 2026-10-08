package com.example.ProjectManagement.controller;

import com.example.ProjectManagement.DTO.Response.ApiResponse;
import com.example.ProjectManagement.DTO.Response.DashboardSummaryResponse;
import com.example.ProjectManagement.DTO.Response.ProjectProgressResponse;
import com.example.ProjectManagement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ApiResponse<DashboardSummaryResponse> getDashboardSummary(Authentication authentication) {
        String username = authentication.getName();
        DashboardSummaryResponse response = dashboardService.getDashboardSummary(username);
        return ApiResponse.success("Dashboard metrics retrieved successfully", response);
    }

    @GetMapping("/projects/{projectId}/progress")
    public ApiResponse<ProjectProgressResponse> getProjectProgress(
            @PathVariable Long projectId,
            Authentication authentication) {
        String username = authentication.getName();
        ProjectProgressResponse response = dashboardService.getProjectProgress(projectId, username);
        return ApiResponse.success("Project progress retrieved successfully", response);
    }
}
