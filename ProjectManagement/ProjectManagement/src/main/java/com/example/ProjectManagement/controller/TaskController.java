package com.example.ProjectManagement.controller;

import com.example.ProjectManagement.DTO.Request.CreateTaskRequest;
import com.example.ProjectManagement.DTO.Request.UpdateDueDateRequest;
import com.example.ProjectManagement.DTO.Request.UpdateTaskStatusRequest;
import com.example.ProjectManagement.DTO.Response.ApiResponse;
import com.example.ProjectManagement.DTO.Response.PagedResponse;
import com.example.ProjectManagement.DTO.Response.TaskResponse;
import com.example.ProjectManagement.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        TaskResponse response = taskService.createTask(request, username);
        return ApiResponse.success("Task created successfully", response);
    }

    @GetMapping("/project/{projectId}")
    public ApiResponse<PagedResponse<TaskResponse>> getTasksForProject(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PagedResponse<TaskResponse> tasks = taskService.getTasksByProjectIdPaged(projectId, page, size, sortBy, sortDir);
        return ApiResponse.success("Tasks retrieved successfully", tasks);
    }

    @GetMapping("/{taskId}")
    public ApiResponse<TaskResponse> getTaskById(@PathVariable Long taskId) {
        TaskResponse task = taskService.getTaskById(taskId);
        return ApiResponse.success("Task retrieved successfully", task);
    }

    @PatchMapping("/{taskId}/status")
    public ApiResponse<TaskResponse> updateTaskStatus(
            @PathVariable Long taskId,
            @Valid
            @RequestBody UpdateTaskStatusRequest request, Authentication authentication) {
        String username = authentication.getName();
        TaskResponse updated = taskService.updateTaskStatus(taskId, request, username);
        return ApiResponse.success("Task status updated successfully", updated);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{taskId}")
    public ApiResponse<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ApiResponse.success("Task deleted successfully", null);
    }

    @PatchMapping("/{taskId}/due-date")
    public ApiResponse<TaskResponse> updateTaskDueDate(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateDueDateRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        TaskResponse response = taskService.updateTaskDueDate(taskId, request, username);
        return ApiResponse.success("Task due date updated successfully", response);
    }
}
