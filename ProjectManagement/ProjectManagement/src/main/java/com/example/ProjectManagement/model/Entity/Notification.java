package com.example.ProjectManagement.model.Entity;


import com.example.ProjectManagement.model.Enum.NotificationTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Data
@Entity
@Table(name = "notification")
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long nId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users userId;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task taskId;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project_id;

    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "message", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationTypeEnum notificationType;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @ManyToOne
    @JoinColumn(name = "sent_by_id")
    private Users sentBy;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @Column(name = "read_at")
    private Instant readAt;
}
