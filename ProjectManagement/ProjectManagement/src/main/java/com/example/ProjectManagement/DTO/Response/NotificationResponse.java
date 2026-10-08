package com.example.ProjectManagement.DTO.Response;

import com.example.ProjectManagement.model.Enum.NotificationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long userId;
    private String username;
    private Long taskId;
    private String taskTitle;
    private Long projectId;
    private String projectName;
    private String title;
    private String message;
    private NotificationTypeEnum notificationType;
    private Long sentByUserId;
    private String sentByUsername;
    private boolean isRead;
    private Instant sentAt;
    private Instant readAt;
    private Instant createdAt;
}
