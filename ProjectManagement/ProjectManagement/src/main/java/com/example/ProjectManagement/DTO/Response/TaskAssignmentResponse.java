package com.example.ProjectManagement.DTO.Response;

import com.example.ProjectManagement.model.Enum.AssignmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TaskAssignmentResponse {
    private Long assignmentId;
    private Long taskId;
    private String taskTitle;
    private String assignedToUsername;
    private String assignedByUsername;
    private AssignmentStatus status;
    private Instant assignedAt;
    private Instant respondedAt;
    private String responseNote;
    private String description;

}
