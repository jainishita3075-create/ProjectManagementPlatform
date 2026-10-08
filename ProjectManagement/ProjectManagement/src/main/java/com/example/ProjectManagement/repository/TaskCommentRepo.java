package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.Task;
import com.example.ProjectManagement.model.Entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskCommentRepo extends JpaRepository<TaskComment, Long> {
    TaskComment findByTcId(Long tcId);

    List<TaskComment> findByTaskIdOrderByCreatedAtAsc(Task taskId);
}
