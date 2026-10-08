package com.example.ProjectManagement.controller;

import com.example.ProjectManagement.DTO.Request.CreateProjectRequest;
import com.example.ProjectManagement.DTO.Request.UpdateProjectRequest;
import com.example.ProjectManagement.DTO.Response.ApiResponse;
import com.example.ProjectManagement.DTO.Response.PagedResponse;
import com.example.ProjectManagement.DTO.Response.ProjectResponse;
import com.example.ProjectManagement.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ApiResponse<ProjectResponse> createProject(
           @Valid
            @RequestBody
            CreateProjectRequest request, Authentication authentication){
        String currentUsername = authentication.getName();
        ProjectResponse response = projectService.createProject(request, currentUsername);
        return ApiResponse.success("Project created Successfully ", response);
    }

    @GetMapping
    public ApiResponse<PagedResponse<ProjectResponse>> getMyProjects(
       @RequestParam(defaultValue = "0") int page,
       @RequestParam(defaultValue = "10") int size,
       @RequestParam(defaultValue = "createdAt") String sortBy,
       @RequestParam(defaultValue = "desc") String sortDir,
        Authentication authentication) {
        String currentUsername = authentication.getName();
        PagedResponse<ProjectResponse> projects = projectService.getUserProjectsPaged(currentUsername, page, size, sortBy, sortDir);
           return ApiResponse.success("Projects retrieved successfully ", projects);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProjectResponse> getProjectById(
            @PathVariable Long id,
            Authentication authentication) {
        String currentUsername = authentication.getName();
        ProjectResponse project = projectService.getProjectById(id, currentUsername);
        return ApiResponse.success("Project retrieved successfully", project);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long id, @RequestBody UpdateProjectRequest request, Authentication authentication){
        ProjectResponse projectResponse = projectService.updateProject(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", projectResponse));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<ProjectResponse>> archiveProject(
            @PathVariable Long id,
            Authentication authentication) {
        ProjectResponse project = projectService.archiveProject(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Project archived successfully", project));
    }
}
