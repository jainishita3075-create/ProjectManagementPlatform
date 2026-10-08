package com.example.ProjectManagement.DTO.Response;

import com.example.ProjectManagement.model.Enum.ProjectPriority;
import com.example.ProjectManagement.model.Enum.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectProgressResponse {
    private Long projectId;
    private String projectName;
    private ProjectStatus status;
    private ProjectPriority priority;
    private long totalTasks;
    private long completedTasks;
    private long inProgressTasks;
    private long pendingTasks;
    private long overdueTasks;
    private double progressPercentage;
    private BigDecimal totalEstimatedHours;
}
