package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.Permissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionsRepo extends JpaRepository<Permissions, Long> {
    Permissions findByPermissionId(Long permissionId);
}
