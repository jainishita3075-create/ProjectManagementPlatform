package com.example.ProjectManagement.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectReportItem {
    private Long projectId;
    private String projectName;
    private String status;
    private long totalTasks;
    private long completedTasks;
    private double progressPercentage;
}
