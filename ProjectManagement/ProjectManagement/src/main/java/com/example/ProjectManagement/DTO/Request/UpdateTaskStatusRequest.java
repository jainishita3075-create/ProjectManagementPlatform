package com.example.ProjectManagement.DTO.Request;

import com.example.ProjectManagement.model.Enum.TaskStatus;
import lombok.Data;

@Data
public class UpdateTaskStatusRequest {
    private TaskStatus status;
}
