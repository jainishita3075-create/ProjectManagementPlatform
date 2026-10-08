package com.example.ProjectManagement.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignTaskRequest {
    @NotNull(message = "Task ID is required")
    private Long taskId;

    @NotBlank(message = "Assigned username is required")
    private String assignedUsername;

    private String description;
}
