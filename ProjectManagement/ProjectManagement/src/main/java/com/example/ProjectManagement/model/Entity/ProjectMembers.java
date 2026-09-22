package com.example.ProjectManagement.model.Entity;

import com.example.ProjectManagement.model.Enum.ProjectMemberRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Entity
@Table(name = "project_members")
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMembers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long pmId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users userId;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project projId;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_role", nullable = false)
    private ProjectMemberRole memberRole = ProjectMemberRole.VIEWER;

    @Column(name = "joined_at")
    private Instant joinedAt;
}
