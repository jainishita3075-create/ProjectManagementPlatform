package com.example.ProjectManagement.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPerformanceItem {
    private Long userId;
    private String username;
    private String name;
    private long tasksAssigned;
    private long tasksCompleted;
    private long tasksOverdue;
    private double completionRate;
}
