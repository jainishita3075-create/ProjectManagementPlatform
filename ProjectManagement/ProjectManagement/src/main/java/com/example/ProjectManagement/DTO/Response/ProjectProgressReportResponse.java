package com.example.ProjectManagement.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectProgressReportResponse {
    private Instant fromDate;
    private Instant toDate;
    private long totalProjectsInScope;
    private long tasksCreatedInPeriod;
    private long tasksCompletedInPeriod;
    private long overdueTasksInPeriod;
    private double overallCompletionRate;
    private List<ProjectReportItem> projectBreakdown;
}
