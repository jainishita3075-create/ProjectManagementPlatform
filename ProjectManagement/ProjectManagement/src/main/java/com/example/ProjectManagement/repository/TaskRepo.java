package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.Project;
import com.example.ProjectManagement.model.Entity.Task;
import com.example.ProjectManagement.model.Enum.TaskStatus;
import com.example.ProjectManagement.model.Entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT DISTINCT t FROM Task t " +
           "LEFT JOIN TaskAssignment ta ON ta.taskId = t " +
           "LEFT JOIN ProjectMembers pm ON pm.projId = t.projId " +
           "WHERE t.projId.owner = :user " +
           "OR pm.userId = :user " +
           "OR ta.userId = :user " +
           "OR t.createdBy = :user")
    List<Task> findAccessibleTasks(@Param("user") Users user);
}
