package com.example.ProjectManagement.DTO.Response;

import com.example.ProjectManagement.model.Enum.InvitationStatus;
import com.example.ProjectManagement.model.Enum.ProjectMemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectInvitationResponse {
    private Long invitationId;
    private Long projectId;
    private String projectName;
    private Long invitedByUserId;
    private String invitedByUsername;
    private Long invitedUserId;
    private String invitedUsername;
    private ProjectMemberRole memberRole;
    private InvitationStatus status;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant respondedAt;

}
