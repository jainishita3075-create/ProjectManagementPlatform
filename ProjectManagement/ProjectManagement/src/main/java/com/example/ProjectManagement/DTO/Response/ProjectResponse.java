package com.example.ProjectManagement.DTO.Response;

import com.example.ProjectManagement.model.Enum.ProjectPriority;
import com.example.ProjectManagement.model.Enum.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponse {
    private Long projectId;
    private String name;
    private String description;
    private String ownerUsername;
    private Instant startDate;
    private Instant endDate;
    private Instant dueDate;
    private ProjectStatus status;
    private ProjectPriority priority;
    private long totalTasks;
    private long completedTasks;
    private double progressPercentage;
    private Instant createdAt;
    private Instant updatedAt;

}
