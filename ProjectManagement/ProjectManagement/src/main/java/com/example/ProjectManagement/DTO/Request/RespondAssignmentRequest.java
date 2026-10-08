package com.example.ProjectManagement.DTO.Request;

import com.example.ProjectManagement.model.Enum.AssignmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RespondAssignmentRequest {
    @NotNull(message = "Assignment status is required")
    private AssignmentStatus status;

    private String responseNote;
}
