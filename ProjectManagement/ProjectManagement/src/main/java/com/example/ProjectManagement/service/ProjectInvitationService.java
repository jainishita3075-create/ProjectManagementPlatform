package com.example.ProjectManagement.service;

import com.example.ProjectManagement.DTO.Request.InviteProjectMemberRequest;
import com.example.ProjectManagement.DTO.Response.ProjectInvitationResponse;
import com.example.ProjectManagement.exception.BadRequestException;
import com.example.ProjectManagement.exception.ResourceNotFoundException;
import com.example.ProjectManagement.exception.UnauthorizedActionException;
import com.example.ProjectManagement.model.Entity.Project;
import com.example.ProjectManagement.model.Entity.ProjectInvitations;
import com.example.ProjectManagement.model.Entity.ProjectMembers;
import com.example.ProjectManagement.model.Entity.Users;
import com.example.ProjectManagement.model.Enum.InvitationStatus;
import com.example.ProjectManagement.model.Enum.NotificationTypeEnum;
import com.example.ProjectManagement.repository.ProjectInvitationRepo;
import com.example.ProjectManagement.repository.ProjectMembersRepo;
import com.example.ProjectManagement.repository.ProjectRepo;
import com.example.ProjectManagement.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectInvitationService {
    private final ProjectInvitationRepo invitationRepo;
    private final ProjectRepo projectRepo;
    private final UserRepo userRepo;
    private final ProjectMembersRepo projectMembersRepo;
    private final NotificationService notificationService;

    @Transactional
    public ProjectInvitationResponse sendInvitation(Long projectId, InviteProjectMemberRequest request, String inviterUsername) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));
        Users inviter = userRepo.findByUserName(inviterUsername);
        if (inviter == null) {
            throw new ResourceNotFoundException("User not found: " + inviterUsername);
        }
        Users invitedUser = userRepo.findByUserName(request.getInvitedUsername());
        if (invitedUser == null) {
            throw new ResourceNotFoundException("Invited user not found: " + request.getInvitedUsername());
        }

        boolean isOwner = project.getOwner().getUserId().equals(inviter.getUserId());
        boolean isAdmin = inviter.getRoles().stream().anyMatch(r -> r.getRole().name().equals("ADMIN"));
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedActionException("Only the project owner or an admin can invite new members.");
        }

        if (projectMembersRepo.existsByProjIdAndUserId(project, invitedUser)) {
            throw new BadRequestException("User " + request.getInvitedUsername() + " is already a member of this project.");
        }

        if (invitationRepo.existsByProjectIdAndInvitedUserIdAndStatus(project, invitedUser, InvitationStatus.PENDING)) {
            throw new BadRequestException("An active invitation is already pending for this user.");
        }

        ProjectInvitations invitation = new ProjectInvitations();
        invitation.setProjectId(project);
        invitation.setInvitedBy(inviter);
        invitation.setInvitedUserId(invitedUser);
        invitation.setMemberRole(request.getMemberRole());
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setCreatedAt(Instant.now());
        invitation.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        ProjectInvitations saved = invitationRepo.save(invitation);

        notificationService.sendNotification(
                invitedUser,
                inviter,
                null,
                project,
                NotificationTypeEnum.PROJECT_INVITATION,
                "Project Invitation",
                "You have been invited to join project '" + project.getName() + "' as " + request.getMemberRole().name()
        );
        return mapToResponse(saved);
    }

    public List<ProjectInvitationResponse> getMyInvitations(String username) {
        Users user = userRepo.findByUserName(username);
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + username);
        }
        return invitationRepo.findByInvitedUserIdAndStatus(user, InvitationStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ProjectInvitationResponse> getProjectInvitations(Long projectId) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));
        return invitationRepo.findByProjectId(project)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectInvitationResponse respondToInvitation(Long invitationId, boolean accept, String username) {
        ProjectInvitations invitation = invitationRepo.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found with ID: " + invitationId));
        Users currentUser = userRepo.findByUserName(username);
        if (currentUser == null || !invitation.getInvitedUserId().getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedActionException("You are not authorized to respond to this invitation.");
        }
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("This invitation has already been processed or expired.");
        }
        if (Instant.now().isAfter(invitation.getExpiresAt())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepo.save(invitation);
            throw new BadRequestException("This invitation has expired.");
        }
        if (accept) {
            invitation.setStatus(InvitationStatus.ACCEPTED);

            if (!projectMembersRepo.existsByProjIdAndUserId(invitation.getProjectId(), currentUser)) {
                ProjectMembers newMember = new ProjectMembers();
                newMember.setProjId(invitation.getProjectId());
                newMember.setUserId(currentUser);
                newMember.setMemberRole(invitation.getMemberRole());
                newMember.setJoinedAt(Instant.now());
                projectMembersRepo.save(newMember);
            }
        } else {
            invitation.setStatus(InvitationStatus.REJECTED);
        }
        invitation.setRespondedAt(Instant.now());
        ProjectInvitations saved = invitationRepo.save(invitation);
        return mapToResponse(saved);
    }
    private ProjectInvitationResponse mapToResponse(ProjectInvitations invite) {
        return ProjectInvitationResponse.builder()
                .invitationId(invite.getProjectInvitationId())
                .projectId(invite.getProjectId().getProjectId())
                .projectName(invite.getProjectId().getName())
                .invitedByUserId(invite.getInvitedBy().getUserId())
                .invitedByUsername(invite.getInvitedBy().getUserName())
                .invitedUserId(invite.getInvitedUserId().getUserId())
                .invitedUsername(invite.getInvitedUserId().getUserName())
                .memberRole(invite.getMemberRole())
                .status(invite.getStatus())
                .expiresAt(invite.getExpiresAt())
                .createdAt(invite.getCreatedAt())
                .respondedAt(invite.getRespondedAt())
                .build();
    }
}
