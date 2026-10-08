package com.example.ProjectManagement.service;

import com.example.ProjectManagement.DTO.Response.TaskActivityResponse;
import com.example.ProjectManagement.exception.ResourceNotFoundException;
import com.example.ProjectManagement.model.Entity.Project;
import com.example.ProjectManagement.model.Entity.Task;
import com.example.ProjectManagement.model.Entity.TaskActivity;
import com.example.ProjectManagement.model.Entity.Users;
import com.example.ProjectManagement.model.Enum.ActionTypeEnum;
import com.example.ProjectManagement.repository.ProjectRepo;
import com.example.ProjectManagement.repository.TaskActivityRepo;
import com.example.ProjectManagement.repository.TaskRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskActivityService {

    private final TaskActivityRepo taskActivityRepo;

    private final TaskRepo taskRepo;

    private final ProjectRepo projectRepo;

    @Transactional
    public void logActivity(Task task, Users user, Project project, ActionTypeEnum actionType, String oldValue, String newValue) {
        TaskActivity activity = new TaskActivity();
        activity.setTask(task);
        activity.setUser(user);
        activity.setProject(project);
        activity.setActionType(actionType);
        activity.setOldValue(oldValue);
        activity.setNewValue(newValue);
        taskActivityRepo.save(activity);
    }

    public List<TaskActivityResponse> getActivitiesForTask(Long taskId) {
        if (!taskRepo.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found with ID: " + taskId);
        }
        return taskActivityRepo.findByTask_TaskIdOrderByCreationTimeDesc(taskId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TaskActivityResponse> getActivitiesForProject(Long projectId) {
        if (!projectRepo.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with ID: " + projectId);
        }
        return taskActivityRepo.findByProject_ProjectIdOrderByCreationTimeDesc(projectId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    private TaskActivityResponse mapToResponse(TaskActivity activity) {
        return TaskActivityResponse.builder()
                .id(activity.getTaId())
                .taskId(activity.getTask() != null ? activity.getTask().getTaskId() : null)
                .taskTitle(activity.getTask() != null ? activity.getTask().getTitle() : null)
                .userId(activity.getUser() != null ? activity.getUser().getUserId() : null)
                .username(activity.getUser() != null ? activity.getUser().getUserName() : null)
                .projectId(activity.getProject() != null ? activity.getProject().getProjectId() : null)
                .actionType(activity.getActionType())
                .oldValue(activity.getOldValue())
                .newValue(activity.getNewValue())
                .creationTime(activity.getCreationTime())
                .build();
    }
}

