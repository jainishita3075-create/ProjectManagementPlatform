package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepo extends JpaRepository<Notification, Long> {
    Notification findBynId(Long nId);
}
