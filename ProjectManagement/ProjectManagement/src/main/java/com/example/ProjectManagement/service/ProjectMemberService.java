package com.example.ProjectManagement.service;

import com.example.ProjectManagement.DTO.Request.AddProjectMemberRequest;
import com.example.ProjectManagement.DTO.Request.UpdateMemberRoleRequest;
import com.example.ProjectManagement.DTO.Response.ProjectMemberResponse;
import com.example.ProjectManagement.exception.BadRequestException;
import com.example.ProjectManagement.exception.ResourceNotFoundException;
import com.example.ProjectManagement.exception.UnauthorizedActionException;
import com.example.ProjectManagement.model.Entity.Project;
import com.example.ProjectManagement.model.Entity.ProjectMembers;
import com.example.ProjectManagement.model.Entity.Users;
import com.example.ProjectManagement.model.Enum.ProjectMemberRole;
import com.example.ProjectManagement.model.Enum.UserRole;
import com.example.ProjectManagement.repository.ProjectMembersRepo;
import com.example.ProjectManagement.repository.ProjectRepo;
import com.example.ProjectManagement.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectMemberService {

    @Autowired
    private ProjectMembersRepo memberRepo;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private UserRepo userRepo;

    public ProjectMemberResponse addMember(Long projectId, AddProjectMemberRequest request, String currentUsername) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        verifyCanManageProject(project, currentUsername);

        Users userToAdd = userRepo.findByUserName(request.getUsername());
        if (userToAdd == null) {
            throw new UsernameNotFoundException("User not found to add: " + request.getUsername());
        }

        if (memberRepo.existsByProjIdAndUserId(project, userToAdd)) {
            throw new BadRequestException("User is already a member of this project.");
        }

        ProjectMembers member = new ProjectMembers();
        member.setProjId(project);
        member.setUserId(userToAdd);
        member.setMemberRole(request.getMemberRole() != null ? request.getMemberRole() : ProjectMemberRole.VIEWER);
        member.setJoinedAt(Instant.now());

        ProjectMembers saved = memberRepo.save(member);
        return mapToResponse(saved);
    }

    public List<ProjectMemberResponse> getProjectMembers(Long projectId, String currentUsername) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        Users currentUser = userRepo.findByUserName(currentUsername);
        boolean isAdmin = currentUser.getRoles() != null && currentUser.getRoles().stream()
                .anyMatch(r -> r.getRole().name().equals("ADMIN"));
        boolean isOwner = project.getOwner().getUserName().equals(currentUsername);
        boolean isMember = memberRepo.existsByProjIdAndUserId(project, currentUser);

        if (!isAdmin && !isOwner && !isMember) {
            throw new UnauthorizedActionException("You are not authorized to view members of this project.");
        }

        return memberRepo.findByProjId(project).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProjectMemberResponse updateMemberRole(Long projectId, Long memberUserId, UpdateMemberRoleRequest request, String currentUsername) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        verifyCanManageProject(project, currentUsername);

        Users targetUser = userRepo.findById(memberUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + memberUserId));

        ProjectMembers member = memberRepo.findByProjIdAndUserId(project, targetUser)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found for user in this project."));

        member.setMemberRole(request.getMemberRole());
        ProjectMembers updated = memberRepo.save(member);
        return mapToResponse(updated);
    }

    public void removeMember(Long projectId, Long memberUserId, String currentUsername) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        verifyCanManageProject(project, currentUsername);

        Users targetUser = userRepo.findById(memberUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + memberUserId));

        ProjectMembers member = memberRepo.findByProjIdAndUserId(project, targetUser)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found for user in this project."));

        memberRepo.delete(member);
    }

    private void verifyCanManageProject(Project project, String currentUsername) {
        Users currentUser = userRepo.findByUserName(currentUsername);
        boolean isAdmin = currentUser.getRoles() != null && currentUser.getRoles().stream()
                .anyMatch(r -> r.getRole() == UserRole.ADMIN);

        boolean isOwner = project.getOwner().getUserName().equals(currentUsername);

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedActionException("Only the project owner or an admin can manage project members.");
        }
    }

    private ProjectMemberResponse mapToResponse(ProjectMembers member) {
        return ProjectMemberResponse.builder()
                .membershipId(member.getPmId())
                .projectId(member.getProjId().getProjectId())
                .projectName(member.getProjId().getName())
                .userId(member.getUserId().getUserId())
                .username(member.getUserId().getUserName())
                .name(member.getUserId().getName())
                .email(member.getUserId().getEmail())
                .memberRole(member.getMemberRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
