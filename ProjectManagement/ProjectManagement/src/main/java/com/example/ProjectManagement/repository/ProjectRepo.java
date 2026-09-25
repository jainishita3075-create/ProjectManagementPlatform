package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepo extends JpaRepository<Project, Long> {
    Project findByProjectId(Long projectId);
}
