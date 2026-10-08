package com.example.ProjectManagement.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class RecurringRuleRequest {
    @NotBlank(message = "Frequency is required (DAILY, WEEKLY, MONTHLY)")
    private String frequency;
    private int intervalValue;
    private Integer dayOfWeek;
    private Integer dayOfMonth;
    @NotNull(message = "Start date is required")
    private Instant startDate;
    private Instant endDate;
}
