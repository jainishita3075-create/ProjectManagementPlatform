package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.TaskActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskActivityRepo extends JpaRepository<TaskActivity, Long> {
    TaskActivity findByTaId(Long taId);
}
