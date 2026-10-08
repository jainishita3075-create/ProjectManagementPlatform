package com.example.ProjectManagement.controller;

import com.example.ProjectManagement.DTO.Request.InviteProjectMemberRequest;
import com.example.ProjectManagement.DTO.Response.ApiResponse;
import com.example.ProjectManagement.DTO.Response.ProjectInvitationResponse;
import com.example.ProjectManagement.service.ProjectInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectInvitationController {

    private final ProjectInvitationService invitationService;

    @PostMapping("/projects/{projectId}/invitations")
    public ResponseEntity<ApiResponse<ProjectInvitationResponse>> sendInvitation(
            @PathVariable Long projectId,
            @Valid @RequestBody InviteProjectMemberRequest request,
            Authentication authentication) {
        ProjectInvitationResponse response = invitationService.sendInvitation(projectId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invitation sent successfully", response));
    }

    @GetMapping("/invitations/my-invitations")
    public ResponseEntity<ApiResponse<List<ProjectInvitationResponse>>> getMyInvitations(Authentication authentication) {
        List<ProjectInvitationResponse> list = invitationService.getMyInvitations(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Pending invitations fetched successfully", list));
    }

    @GetMapping("/projects/{projectId}/invitations")
    public ResponseEntity<ApiResponse<List<ProjectInvitationResponse>>> getProjectInvitations(@PathVariable Long projectId) {
        List<ProjectInvitationResponse> list = invitationService.getProjectInvitations(projectId);
        return ResponseEntity.ok(ApiResponse.success("Project invitations fetched successfully", list));
    }

    @PatchMapping("/invitations/{invitationId}/respond")
    public ResponseEntity<ApiResponse<ProjectInvitationResponse>> respondToInvitation(
            @PathVariable Long invitationId,
            @RequestParam("accept") boolean accept,
            Authentication authentication) {
        ProjectInvitationResponse response = invitationService.respondToInvitation(invitationId, accept, authentication.getName());
        String actionText = accept ? "accepted" : "rejected";
        return ResponseEntity.ok(ApiResponse.success("Invitation " + actionText + " successfully", response));
    }
}
