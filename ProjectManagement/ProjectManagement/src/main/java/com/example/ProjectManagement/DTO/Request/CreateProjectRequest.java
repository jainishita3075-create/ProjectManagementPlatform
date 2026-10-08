package com.example.ProjectManagement.DTO.Request;

import com.example.ProjectManagement.model.Enum.ProjectPriority;
import com.example.ProjectManagement.model.Enum.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateProjectRequest {
    @NotBlank(message = "Project name is required")
    private String name;

    private String description;
    private Instant startDate;
    private Instant endDate;

    @NotNull(message = "Due date is required")
    private Instant dueDate;

    private ProjectStatus status = ProjectStatus.ACTIVE;
    private ProjectPriority priority = ProjectPriority.MEDIUM;
}
