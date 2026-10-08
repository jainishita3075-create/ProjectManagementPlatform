package com.example.ProjectManagement.DTO.Response;

import com.example.ProjectManagement.model.Enum.ProjectPriority;
import com.example.ProjectManagement.model.Enum.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponse {
    private Long taskId;
    private Long projectId;
    private String projectName;
    private String title;
    private String description;
    private TaskStatus status;
    private ProjectPriority priority;
    private String createdByUsername;
    private Instant startDate;
    private Instant dueDate;
    private Instant completionDate;
    private BigDecimal estimatedEffortHours;
    private Instant createdAt;
    private Instant updatedAt;
    private Long assigneeUserId;
    private String asigneeUsername;
    private String asigneeName;
}
