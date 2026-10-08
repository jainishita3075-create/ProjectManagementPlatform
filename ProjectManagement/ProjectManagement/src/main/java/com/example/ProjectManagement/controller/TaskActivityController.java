package com.example.ProjectManagement.controller;

import com.example.ProjectManagement.DTO.Response.ApiResponse;
import com.example.ProjectManagement.DTO.Response.TaskActivityResponse;
import com.example.ProjectManagement.service.TaskActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskActivityController {

    private final TaskActivityService taskActivityService;

    @GetMapping("/tasks/{taskId}/activities")
    public ResponseEntity<ApiResponse<List<TaskActivityResponse>>> getTaskActivities(@PathVariable Long taskId) {
        List<TaskActivityResponse> activities = taskActivityService.getActivitiesForTask(taskId);
        return ResponseEntity.ok(ApiResponse.success("Task activity log fetched successfully", activities));
    }

    @GetMapping("/projects/{projectId}/activities")
    public ResponseEntity<ApiResponse<List<TaskActivityResponse>>> getProjectActivities(@PathVariable Long projectId) {
        List<TaskActivityResponse> activities = taskActivityService.getActivitiesForProject(projectId);
        return ResponseEntity.ok(ApiResponse.success("Project activity log fetched successfully", activities));
    }
}
