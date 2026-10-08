package com.example.ProjectManagement.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryResponse {
    private long totalProjects;
    private long activeProjects;
    private long completedProjects;
    private long totalTasks;
    private long completedTasks;
    private long inProgressTasks;
    private long overdueTasks;
    private List<UserWorkloadResponse> workloadByUser;
}
