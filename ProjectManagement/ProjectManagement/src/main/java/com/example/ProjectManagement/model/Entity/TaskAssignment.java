package com.example.ProjectManagement.model.Entity;


import com.example.ProjectManagement.model.Enum.AssignmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Entity
@Table(name = "task_assignment")
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
    @Column(name = "status")
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
