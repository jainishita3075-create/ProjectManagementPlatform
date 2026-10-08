package com.example.ProjectManagement.DTO.Request;

import com.example.ProjectManagement.model.Enum.ProjectMemberRole;
import lombok.Data;

@Data
public class AddProjectMemberRequest {
    private String username;
    private ProjectMemberRole memberRole;
}
