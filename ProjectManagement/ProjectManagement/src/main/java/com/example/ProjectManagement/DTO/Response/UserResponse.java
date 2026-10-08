package com.example.ProjectManagement.DTO.Response;

import com.example.ProjectManagement.model.Enum.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserResponse {
    private Long userId;
    private String name;
    private String email;
    private String username;
    private boolean active;
    private Set<UserRole> roles;
}
