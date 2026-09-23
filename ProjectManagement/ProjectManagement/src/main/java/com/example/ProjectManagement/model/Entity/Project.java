package com.example.ProjectManagement.model.Entity;

import com.example.ProjectManagement.model.Enum.ProjectPriority;
import com.example.ProjectManagement.model.Enum.ProjectStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="projects",
        indexes = {
        @Index(name = "idx_owner_id", columnList = "owner_id", unique = true),
        @Index(name = "idx_status", columnList = "status")}
)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long projectId;

    @Column(name = "project_name",nullable = false)
    private String name;
    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "owner_id",nullable = false)
    private Users owner_id;
    @Column(name = "start_date")
    private Instant startDate;
    @Column(name = "end_date")
    private Instant endDate;
    @Column(name = "due_date")
    private Instant dueDate;

    @ColumnDefault(value = "'ACTIVE'")
    @Enumerated(EnumType.STRING)
    @Column(name = "status" ,nullable = false)
    private ProjectStatus status;

    @ColumnDefault(value = "'MEDIUM'")
    @Enumerated(EnumType.STRING)
    @Column(name = "priority",nullable = false)
    private ProjectPriority priority;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at",nullable = false)
    private Instant updatedAt;

}
