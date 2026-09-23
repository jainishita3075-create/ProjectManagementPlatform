package com.example.ProjectManagement.model.Entity;


import com.example.ProjectManagement.model.Enum.AssignmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Entity
@Table(name = "task_assignment",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_task_assignment_user", columnNames = {"task_id", "user_id"})
        },
indexes = {@Index(name = "idx_task_assign_task_id", columnList = "task_id"),
        @Index(name = "idx_task_assign_user_id", columnList = "user_id"),
        @Index(name = "idx_assigned_by", columnList = "assigned_by"),
        @Index(name = "idx_assignment_status", columnList = "assignment_status")
})
@AllArgsConstructor
@NoArgsConstructor
public class TaskAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long taskAssignmentId;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task taskId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users userId;

    @ManyToOne
    @JoinColumn(name = "assigned_by", nullable = false)
    private Users assignedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_status")
    private AssignmentStatus status = AssignmentStatus.PENDING;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "response_note")
    private String responseNote;

    @Column(name = "description")
    private String description;
}
