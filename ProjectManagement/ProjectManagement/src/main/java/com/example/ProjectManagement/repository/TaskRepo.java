package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.Project;
import com.example.ProjectManagement.model.Entity.Task;
import com.example.ProjectManagement.model.Enum.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TaskRepo extends JpaRepository<Task, Long> {
    Task findByTaskId(Long taskId);
    List<Task> findByProjId(Project project);
    List<Task> findByProjIdAndStatus(Project project, TaskStatus status);
    Page<Task> findByProjId(Project project, Pageable pageable);
    List<Task> findByCreatedAtBetween(Instant from, Instant to);
    List<Task> findByProjIdAndCreatedAtBetween(Project project, Instant from, Instant to);
}
