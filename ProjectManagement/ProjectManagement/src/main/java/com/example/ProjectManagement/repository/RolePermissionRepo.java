package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.RolePermissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepo extends JpaRepository<RolePermissions, Long> {
    RolePermissions findByRolePermissionId(Long rolePermissionId);
}
