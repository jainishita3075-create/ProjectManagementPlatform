package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.Project;
import com.example.ProjectManagement.model.Entity.ProjectInvitations;
import com.example.ProjectManagement.model.Entity.Users;
import com.example.ProjectManagement.model.Enum.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectInvitationRepo extends JpaRepository<ProjectInvitations,Long> {
    List<ProjectInvitations> findByInvitedUserIdAndStatus(Users invitedUser, InvitationStatus status);
    List<ProjectInvitations> findByProjectId(Project project);
    boolean existsByProjectIdAndInvitedUserIdAndStatus(Project project, Users invitedUser, InvitationStatus status);
}
