package com.example.ProjectManagement.service;

import com.example.ProjectManagement.DTO.Response.NotificationResponse;
import com.example.ProjectManagement.exception.ResourceNotFoundException;
import com.example.ProjectManagement.exception.UnauthorizedActionException;
import com.example.ProjectManagement.model.Entity.Notification;
import com.example.ProjectManagement.model.Entity.Project;
import com.example.ProjectManagement.model.Entity.Task;
import com.example.ProjectManagement.model.Entity.Users;
import com.example.ProjectManagement.model.Enum.NotificationTypeEnum;
import com.example.ProjectManagement.repository.NotificationRepo;
import com.example.ProjectManagement.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepo notificationRepo;
    private final UserRepo userRepo;


    @Transactional
    public void sendNotification(Users recipient, Users sender, Task task, Project project,
                                 NotificationTypeEnum type, String title, String message) {
        // Prevent sending notification to oneself
        if (sender != null && recipient.getUserId().equals(sender.getUserId())) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(recipient);
        notification.setSentBy(sender);
        notification.setTaskId(task);
        notification.setProject_id(project);
        notification.setNotificationType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setSentAt(Instant.now());
        notification.setRead(false);
        notificationRepo.save(notification);
    }
    /**
     * Get all notifications for current user
     */
    public List<NotificationResponse> getMyNotifications(String username, boolean unreadOnly) {
        Users user = userRepo.findByUserName(username);
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + username);
        }
        List<Notification> list = unreadOnly
                ? notificationRepo.findByUserId_UserIdAndIsReadFalseOrderByCreatedAtDesc(user.getUserId())
                : notificationRepo.findByUserId_UserIdOrderByCreatedAtDesc(user.getUserId());
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public long getUnreadCount(String username) {
        Users user = userRepo.findByUserName(username);
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + username);
        }
        return notificationRepo.countByUserId_UserIdAndIsReadFalse(user.getUserId());
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, String username) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));
        Users currentUser = userRepo.findByUserName(username);
        if (currentUser == null || !notification.getUserId().getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedActionException("You can only mark your own notifications as read.");
        }
        notification.setRead(true);
        notification.setReadAt(Instant.now());
        Notification updated = notificationRepo.save(notification);
        return mapToResponse(updated);
    }

    @Transactional
    public void markAllAsRead(String username) {
        Users user = userRepo.findByUserName(username);
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + username);
        }
        List<Notification> unreadList = notificationRepo.findByUserId_UserIdAndIsReadFalseOrderByCreatedAtDesc(user.getUserId());
        Instant now = Instant.now();
        for (Notification n : unreadList) {
            n.setRead(true);
            n.setReadAt(now);
        }
        notificationRepo.saveAll(unreadList);
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getNId())
                .userId(n.getUserId() != null ? n.getUserId().getUserId() : null)
                .username(n.getUserId() != null ? n.getUserId().getUserName() : null)
                .taskId(n.getTaskId() != null ? n.getTaskId().getTaskId() : null)
                .taskTitle(n.getTaskId() != null ? n.getTaskId().getTitle() : null)
                .projectId(n.getProject_id() != null ? n.getProject_id().getProjectId() : null)
                .projectName(n.getProject_id() != null ? n.getProject_id().getName() : null)
                .title(n.getTitle())
                .message(n.getMessage())
                .notificationType(n.getNotificationType())
                .sentByUserId(n.getSentBy() != null ? n.getSentBy().getUserId() : null)
                .sentByUsername(n.getSentBy() != null ? n.getSentBy().getUserName() : null)
                .isRead(n.isRead())
                .sentAt(n.getSentAt())
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .build();
    }

}
