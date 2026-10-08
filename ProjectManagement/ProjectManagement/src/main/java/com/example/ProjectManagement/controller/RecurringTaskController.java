package com.example.ProjectManagement.controller;

import com.example.ProjectManagement.DTO.Request.RecurringRuleRequest;
import com.example.ProjectManagement.DTO.Response.ApiResponse;
import com.example.ProjectManagement.DTO.Response.RecurringRuleResponse;
import com.example.ProjectManagement.service.RecurringTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class RecurringTaskController {

    private final RecurringTaskService recurringTaskService;

    @PostMapping("/{taskId}/recurring-rule")
    public ResponseEntity<ApiResponse<RecurringRuleResponse>> setRecurringRule(
            @PathVariable Long taskId,
            @Valid @RequestBody RecurringRuleRequest request,
            Authentication authentication) {
        RecurringRuleResponse response = recurringTaskService.createOrUpdateRule(taskId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recurring rule configured successfully", response));
    }

    @GetMapping("/{taskId}/recurring-rule")
    public ResponseEntity<ApiResponse<RecurringRuleResponse>> getRecurringRule(@PathVariable Long taskId) {
        RecurringRuleResponse response = recurringTaskService.getRuleForTask(taskId);
        return ResponseEntity.ok(ApiResponse.success("Recurring rule fetched successfully", response));
    }

    @DeleteMapping("/{taskId}/recurring-rule")
    public ResponseEntity<ApiResponse<Void>> deleteRecurringRule(@PathVariable Long taskId) {
        recurringTaskService.deactivateRule(taskId);
        return ResponseEntity.ok(ApiResponse.success("Recurring rule deactivated successfully", null));
    }
}
