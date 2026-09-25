package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepo extends JpaRepository<Task, Long> {
    Task findByTaskId(Long taskId);
}
