package com.example.ProjectManagement.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecurringRuleResponse {
    private Long id;
    private Long taskId;
    private String taskTitle;
    private String frequency;
    private int intervalValue;
    private Integer dayOfWeek;
    private Integer dayOfMonth;
    private Instant startDate;
    private Instant endDate;
    private Instant nextRunAt;
    private boolean isActive;
    private Instant createdAt;
}
