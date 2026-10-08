package com.example.ProjectManagement.DTO.Response;

import com.example.ProjectManagement.model.Enum.ActionTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskActivityResponse {
    private Long id;
    private Long taskId;
    private String taskTitle;
    private Long userId;
    private String username;
    private Long projectId;
    private ActionTypeEnum actionType;
    private String oldValue;
    private String newValue;
    private Instant creationTime;
}
