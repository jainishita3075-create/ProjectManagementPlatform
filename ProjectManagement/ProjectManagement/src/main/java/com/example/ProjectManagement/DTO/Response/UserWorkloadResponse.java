package com.example.ProjectManagement.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserWorkloadResponse {
    private Long userId;
    private String username;
    private String name;
    private long totalAssignedTasks;
    private long completedTasks;
    private long pendingTasks;
    private long overdueTasks;
}
