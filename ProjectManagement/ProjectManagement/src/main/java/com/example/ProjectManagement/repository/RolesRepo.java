package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolesRepo extends JpaRepository<Roles, Long> {
    Roles findByRoleId(Long roleId);
}
