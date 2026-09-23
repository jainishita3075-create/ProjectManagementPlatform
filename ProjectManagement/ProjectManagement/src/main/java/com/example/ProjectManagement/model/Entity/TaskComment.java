package com.example.ProjectManagement.model.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "task_comment",
indexes = {
        @Index(name = "idx_task_comm_task_id", columnList = "task_id"),
        @Index(name = "idx_task_comm_user_id", columnList = "user_id"),
        @Index(name = "idx_parent_comment_id", columnList = "parent_comment_id")
})
@AllArgsConstructor
@NoArgsConstructor
public class TaskComment {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tcId;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task taskId;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private TaskComment parentCommentId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users userId;

    @Column(name = "comment")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
