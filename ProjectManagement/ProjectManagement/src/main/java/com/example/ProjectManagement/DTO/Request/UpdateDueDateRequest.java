package com.example.ProjectManagement.DTO.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class UpdateDueDateRequest {
    @NotNull(message = "Due date is required")
    private Instant dueDate;
}
