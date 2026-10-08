package com.example.ProjectManagement.DTO.Request;

import com.example.ProjectManagement.model.Enum.ProjectMemberRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InviteProjectMemberRequest {
    @NotBlank(message = "Invalid username is required")
    private String invitedUsername;

    @NotNull(message = "Member role is required")
    private ProjectMemberRole memberRole = ProjectMemberRole.VIEWER;
}
