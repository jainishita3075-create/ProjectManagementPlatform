package com.example.ProjectManagement.DTO.Response;

import com.example.ProjectManagement.model.Enum.ProjectMemberRole;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ProjectMemberResponse {
    private Long membershipId;
    private Long projectId;
    private String projectName;
    private Long userId;
    private String username;
    private String name;
    private String email;
    private ProjectMemberRole memberRole;
    private Instant joinedAt;
}
