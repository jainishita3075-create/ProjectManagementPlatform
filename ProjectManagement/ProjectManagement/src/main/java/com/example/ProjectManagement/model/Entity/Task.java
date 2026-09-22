package com.example.ProjectManagement.model.Entity;

import com.example.ProjectManagement.model.Enum.ProjectPriority;
import com.example.ProjectManagement.model.Enum.TaskStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long taskId;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project projId;

    @ManyToOne
    @JoinColumn(name = "parent_task_id")
    private Task parentTaskId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TaskStatus status = TaskStatus.ASSIGNED;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private ProjectPriority priority = ProjectPriority.MEDIUM;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Users createdBy;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "due_date", nullable = false)
    private Instant dueDate;

    @Column(name = "completion_date")
    private Instant completion_date;

    @Column(name = "is_recurring", nullable = false)
    private boolean recurring = false;

    @ManyToOne
    @JoinColumn(name = "archived_by")
    private Users archivedBy;

    @Column(name = "archived_at")
    private Instant archivedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
