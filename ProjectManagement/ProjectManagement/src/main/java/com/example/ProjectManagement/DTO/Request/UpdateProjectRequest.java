package com.example.ProjectManagement.DTO.Request;

import com.example.ProjectManagement.model.Enum.ProjectPriority;
import com.example.ProjectManagement.model.Enum.ProjectStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class UpdateProjectRequest {
    private String name;
    private String description;
    private Instant startDate;
    private Instant endDate;
    private Instant dueDate;
    private ProjectStatus status;
    private ProjectPriority priority;
}
