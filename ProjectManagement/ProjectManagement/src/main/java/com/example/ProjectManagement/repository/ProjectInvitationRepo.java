package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.Project;
import com.example.ProjectManagement.model.Entity.ProjectInvitations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectInvitationRepo extends JpaRepository<ProjectInvitations,Long> {
    ProjectInvitations findByProjectId(Project projectId);
}
