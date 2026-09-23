package com.example.ProjectManagement.model.Entity;


import com.example.ProjectManagement.model.Enum.InvitationStatus;
import com.example.ProjectManagement.model.Enum.ProjectMemberRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Data
@Entity
@Table(name = "project_invitation",
indexes = {
        @Index(name = "idx_invite_project_id", columnList = "project_id"),
        @Index(name = "idx_invited_user_id", columnList = "invited_user_id"),
        @Index(name = "idx_invited_by", columnList = "invited_by"),
        @Index(name = "idx_invitation_status", columnList = "invitation_status")
})
@AllArgsConstructor
@NoArgsConstructor
public class ProjectInvitations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long projectInvitationId;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project projectId;

    @ManyToOne
    @JoinColumn(name = "invited_by", nullable = false)
    private Users invitedBy;

    @ManyToOne
    @JoinColumn(name = "invited_user_id", nullable = false)
    private Users invitedUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_roles", nullable = false)
    private ProjectMemberRole memberRole = ProjectMemberRole.VIEWER;

    @Enumerated(EnumType.STRING)
    @Column(name = "invitation_status", nullable = false)
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "responded_at")
    private Instant respondedAt;
}
