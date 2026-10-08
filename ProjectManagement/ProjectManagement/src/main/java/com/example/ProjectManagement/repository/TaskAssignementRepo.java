package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.Task;
import com.example.ProjectManagement.model.Entity.TaskAssignment;
import com.example.ProjectManagement.model.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskAssignementRepo extends JpaRepository<TaskAssignment, Long> {
    TaskAssignment findByTaskAssignmentId(Long taskAssignmentId);

    List<TaskAssignment> findByUserId(Users userId);

    List<TaskAssignment> findByTaskId(Task taskId);

    boolean existsByTaskIdAndUserId(Task task, Users users);
}
