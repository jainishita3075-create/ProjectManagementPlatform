package com.example.ProjectManagement.controller;

import com.example.ProjectManagement.DTO.Request.AssignTaskRequest;
import com.example.ProjectManagement.DTO.Request.RespondAssignmentRequest;
import com.example.ProjectManagement.DTO.Response.ApiResponse;
import com.example.ProjectManagement.DTO.Response.TaskAssignmentResponse;
import com.example.ProjectManagement.service.TaskAssignmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task-assignments")
public class TaskAssignmentController {

    @Autowired
    private TaskAssignmentService assignmentService;

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ApiResponse<TaskAssignmentResponse> assignTask(
            @Valid
            @RequestBody AssignTaskRequest request, Authentication authentication){
        String assignedByUsername = authentication.getName();
        TaskAssignmentResponse response = assignmentService.assignTask(request, assignedByUsername);
        return ApiResponse.success("Task assigned successfully", response);
    }

    @GetMapping("/my-assignments")
    public ApiResponse<List<TaskAssignmentResponse>> getMyAssignments(Authentication authentication) {
        String username = authentication.getName();
        List<TaskAssignmentResponse> assignments = assignmentService.getMyAssignments(username);
        return ApiResponse.success("Assignments retrieved successfully", assignments);
    }

    @GetMapping("/task/{taskId}")
    public ApiResponse<List<TaskAssignmentResponse>> getAssignmentsForTask(@PathVariable Long taskId) {
        List<TaskAssignmentResponse> assignments = assignmentService.getAssignmentsByTaskId(taskId);
        return ApiResponse.success("Task assignments retrieved successfully", assignments);
    }

    @PatchMapping("/{assignmentId}/respond")
    public ApiResponse<TaskAssignmentResponse> respondToAssignment(
            @PathVariable Long assignmentId,
            @Valid
            @RequestBody RespondAssignmentRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        TaskAssignmentResponse response = assignmentService.respondToAssignment(assignmentId, request, username);
        return ApiResponse.success("Assignment status updated successfully", response);
    }
}

