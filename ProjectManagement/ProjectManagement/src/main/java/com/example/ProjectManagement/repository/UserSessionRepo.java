package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionRepo extends JpaRepository<UserSession, Long> {
    UserSession findByUserSessionId(Long userSessionId);
}
