package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<Users, Long> {
    Users findByUserName(String username);
}
