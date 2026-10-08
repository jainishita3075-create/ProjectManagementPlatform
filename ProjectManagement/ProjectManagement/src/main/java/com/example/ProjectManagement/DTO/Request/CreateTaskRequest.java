package com.example.ProjectManagement.DTO.Request;

import com.example.ProjectManagement.model.Enum.ProjectPriority;
import com.example.ProjectManagement.model.Enum.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class CreateTaskRequest {
    @NotNull(message = "Project Id is required")
    private Long projectId;
    @NotBlank(message = "Task title is required")
    private String title;
    private String description;
    private ProjectPriority priority = ProjectPriority.MEDIUM;
    private TaskStatus status = TaskStatus.ASSIGNED;
    private BigDecimal estimatedEffortHours = BigDecimal.ZERO;
    private Instant startDate;
    @NotNull(message = "Due Date is required")
    private Instant dueDate;
    private String assignedUsername;
}
