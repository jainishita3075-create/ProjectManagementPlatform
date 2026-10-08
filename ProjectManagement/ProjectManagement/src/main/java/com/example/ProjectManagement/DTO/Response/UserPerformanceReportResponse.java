package com.example.ProjectManagement.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPerformanceReportResponse {
    private Instant fromDate;
    private Instant toDate;
    private List<UserPerformanceItem> userPerformanceList;
}
