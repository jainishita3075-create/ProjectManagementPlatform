package com.example.ProjectManagement.DTO.Request;

import com.example.ProjectManagement.model.Enum.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRoleRequest {
    @NotNull(message = "Role is required (ADMIN, MANAGER, MEMBER)")
    private UserRole role;
}
