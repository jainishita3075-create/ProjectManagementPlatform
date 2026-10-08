package com.example.ProjectManagement.controller;

import com.example.ProjectManagement.DTO.Request.AddProjectMemberRequest;
import com.example.ProjectManagement.DTO.Request.UpdateMemberRoleRequest;
import com.example.ProjectManagement.DTO.Response.ApiResponse;
import com.example.ProjectManagement.DTO.Response.ProjectMemberResponse;
import com.example.ProjectManagement.service.ProjectMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

    @Autowired
    private ProjectMemberService memberService;

    @PostMapping
    public ApiResponse<ProjectMemberResponse> addMember(
            @PathVariable Long projectId,
            @RequestBody AddProjectMemberRequest request,
            Authentication authentication) {
        String currentUsername = authentication.getName();
        ProjectMemberResponse response = memberService.addMember(projectId, request, currentUsername);
        return ApiResponse.success("Member added to project successfully", response);
    }

    @GetMapping
    public ApiResponse<List<ProjectMemberResponse>> getMembers(
            @PathVariable Long projectId,
            Authentication authentication) {
        String currentUsername = authentication.getName();
        List<ProjectMemberResponse> members = memberService.getProjectMembers(projectId, currentUsername);
        return ApiResponse.success("Project members retrieved successfully", members);
    }

    @PatchMapping("/{userId}")
    public ApiResponse<ProjectMemberResponse> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @RequestBody UpdateMemberRoleRequest request,
            Authentication authentication) {
        String currentUsername = authentication.getName();
        ProjectMemberResponse response = memberService.updateMemberRole(projectId, userId, request, currentUsername);
        return ApiResponse.success("Member role updated successfully", response);
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            Authentication authentication) {
        String currentUsername = authentication.getName();
        memberService.removeMember(projectId, userId, currentUsername);
        return ApiResponse.success("Member removed from project successfully", null);
    }
}
