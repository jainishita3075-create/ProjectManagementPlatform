package com.example.ProjectManagement.controller;

import com.example.ProjectManagement.DTO.Response.ApiResponse;
import com.example.ProjectManagement.DTO.Response.NotificationResponse;
import com.example.ProjectManagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(
            @RequestParam(value = "unreadOnly", defaultValue = "false") boolean unreadOnly,
            Authentication authentication) {
        List<NotificationResponse> notifications = notificationService.getMyNotifications(authentication.getName(), unreadOnly);
        return ResponseEntity.ok(ApiResponse.success("Notifications fetched successfully", notifications));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(Authentication authentication) {
        long count = notificationService.getUnreadCount(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Unread count fetched", Map.of("unreadCount", count)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        NotificationResponse response = notificationService.markAsRead(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", response));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication authentication) {
        notificationService.markAllAsRead(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }
}
