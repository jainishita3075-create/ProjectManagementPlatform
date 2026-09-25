package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAssignementRepo extends JpaRepository<TaskAssignment, Long> {
    TaskAssignment findByTaskAssignmentId(Long taskAssignmentId);
}
