package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.Project;
import com.example.ProjectManagement.model.Entity.ProjectMembers;
import com.example.ProjectManagement.model.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMembersRepo extends JpaRepository<ProjectMembers, Long> {
    ProjectMembers findByPmId(Long pmId);
    List<ProjectMembers> findByProjId(Project project);
    List<ProjectMembers> findByUserId(Users user);
    Optional<ProjectMembers> findByProjIdAndUserId(Project project, Users user);
    boolean existsByProjIdAndUserId(Project project, Users user);
}
