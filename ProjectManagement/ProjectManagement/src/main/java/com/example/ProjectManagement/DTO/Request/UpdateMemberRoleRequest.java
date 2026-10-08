package com.example.ProjectManagement.DTO.Request;

import com.example.ProjectManagement.model.Enum.ProjectMemberRole;
import lombok.Data;

@Data
public class UpdateMemberRoleRequest {
    private ProjectMemberRole memberRole;
}
