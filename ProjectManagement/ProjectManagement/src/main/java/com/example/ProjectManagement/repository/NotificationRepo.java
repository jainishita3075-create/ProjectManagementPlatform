package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long> {
    Notification findBynId(Long nId);
    List<Notification> findByUserId_UserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserId_UserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    long countByUserId_UserIdAndIsReadFalse(Long userId);
}
