package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.TaskAttachments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskAttachmentRepo extends JpaRepository<TaskAttachments, Long> {
    TaskAttachments findByTaskAttachmentId(Long taskAttachmentId);
    List<TaskAttachments> findByTaskId_TaskIdOrderByUploadedAtDesc(Long taskId);
}
