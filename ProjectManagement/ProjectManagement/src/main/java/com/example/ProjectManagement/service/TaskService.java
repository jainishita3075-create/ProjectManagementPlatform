package com.example.ProjectManagement.service;

import com.example.ProjectManagement.DTO.Request.CreateTaskRequest;
import com.example.ProjectManagement.DTO.Request.UpdateDueDateRequest;
import com.example.ProjectManagement.DTO.Request.UpdateTaskStatusRequest;
import com.example.ProjectManagement.DTO.Response.PagedResponse;
import com.example.ProjectManagement.DTO.Response.TaskResponse;
import com.example.ProjectManagement.exception.BadRequestException;
import com.example.ProjectManagement.exception.ResourceNotFoundException;
import com.example.ProjectManagement.exception.UnauthorizedActionException;
import com.example.ProjectManagement.model.Entity.Project;
import com.example.ProjectManagement.model.Entity.Task;
import com.example.ProjectManagement.model.Entity.TaskAssignment;
import com.example.ProjectManagement.model.Entity.Users;
import com.example.ProjectManagement.model.Enum.*;
import com.example.ProjectManagement.repository.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TaskAssignementRepo assignementRepo;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TaskActivityService taskActivityService;

    @Autowired
    private ProjectMembersRepo projectMembersRepo;

    @Transactional
    @CacheEvict(value = "projectTasks", allEntries = true)
    public TaskResponse createTask(CreateTaskRequest request, String username) {
        Users user = userRepo.findByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        Project project = projectRepo.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + request.getProjectId()));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getRole().name().equals("ADMIN"));
        boolean isOwner = project.getOwner() != null && project.getOwner().getUserId().equals(user.getUserId());
        boolean isMember = projectMembersRepo.existsByProjIdAndUserId(project, user);
        if (!isAdmin && !isOwner && !isMember) {
            throw new UnauthorizedActionException("You are not authorized to create tasks for this project.");
        }

        if (request.getDueDate() == null) {
            throw new BadRequestException("Due date is required for creating a task.");
        }

        Task task = new Task();
        task.setProjId(project);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCreatedBy(user);
        task.setPriority(request.getPriority() != null ? request.getPriority() : ProjectPriority.MEDIUM);
        task.setStatus(request.getStatus() != null ? request.getStatus() : TaskStatus.ASSIGNED);
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());
        task.setEstimatedEffortHours(request.getEstimatedEffortHours() != null ? request.getEstimatedEffortHours(): BigDecimal.ZERO);

        Task saved = taskRepo.save(task);
        if (request.getAssignedUsername() != null && !request.getAssignedUsername().isBlank()) {
            Users assignee = userRepo.findByUserName(request.getAssignedUsername());
            if (assignee != null) {
                TaskAssignment assignment = new TaskAssignment();
                assignment.setTaskId(saved);
                assignment.setUserId(assignee);
                assignment.setAssignedBy(user);
                assignment.setStatus(AssignmentStatus.PENDING);
                assignment.setAssignedAt(Instant.now());
                assignment.setDescription("Assigned during task creation");
                assignementRepo.save(assignment);

                notificationService.sendNotification(
                        assignee,
                        user,
                        saved,
                        project,
                        NotificationTypeEnum.TASK_ASSIGNED,
                        "New Task Assigned",
                        "You have been assigned to task: " + saved.getTitle()
                );
            }
        }

        return mapToResponse(saved);
    }

    public PagedResponse<TaskResponse> getTasksByProjectIdPaged(Long projectId, int page, int size, String sortBy, String sortDir) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Task> taskPage = taskRepo.findByProjId(project, pageable);
        List<TaskResponse> content = taskPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PagedResponse.of(taskPage, content);
    }

    @Cacheable(value = "projectTasks", key = "#projectId")
    public List<TaskResponse> getTasksByProjectId(Long projectId) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        return taskRepo.findByProjId(project).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse getTaskById(Long taskId) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        return mapToResponse(task);
    }

    @Transactional
    @CacheEvict(value = "projectTasks", allEntries = true)
    public TaskResponse updateTaskStatus(Long taskId, UpdateTaskStatusRequest request, String username) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        Users currentUser = userRepo.findByUserName(username);
        if (currentUser == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getRole().name().equals("ADMIN"));
        boolean isOwner = task.getProjId().getOwner() != null && task.getProjId().getOwner().getUserId().equals(currentUser.getUserId());
        boolean isCreator = task.getCreatedBy() != null && task.getCreatedBy().getUserId().equals(currentUser.getUserId());
        boolean isAssignee = assignementRepo.existsByTaskIdAndUserId(task, currentUser);
        boolean isProjectMember = projectMembersRepo.existsByProjIdAndUserId(task.getProjId(), currentUser);
        boolean isManager = currentUser.getRoles().stream().anyMatch(r -> r.getRole().name().equals("MANAGER"))
                && (isOwner || isProjectMember);
        if (!isAdmin && !isOwner && !isCreator && !isAssignee && !isManager) {
            throw new UnauthorizedActionException("You are not authorized to update this task's status.");
        }
        TaskStatus oldStatus = task.getStatus();
        if (request.getStatus() != null && !request.getStatus().equals(oldStatus)) {
            task.setStatus(request.getStatus());
            if (request.getStatus() == TaskStatus.COMPLETED) {
                task.setCompletion_date(Instant.now());
            }
            Task updated = taskRepo.save(task);
            taskActivityService.logActivity(
                    updated,
                    currentUser,
                    updated.getProjId(),
                    ActionTypeEnum.STATUS_CHANGED,
                    oldStatus != null ? oldStatus.name() : null,
                    request.getStatus().name()
            );
            return mapToResponse(updated);
        }
        return mapToResponse(task);
    }

    @Transactional
    @CacheEvict(value = "projectTasks", allEntries = true)
    public TaskResponse updateTaskDueDate(Long taskId, UpdateDueDateRequest request, String username) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        Users currentUser = userRepo.findByUserName(username);
        if (currentUser == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getRole().name().equals("ADMIN"));
        boolean isOwner = task.getProjId().getOwner() != null && task.getProjId().getOwner().getUserId().equals(currentUser.getUserId());
        boolean isCreator = task.getCreatedBy() != null && task.getCreatedBy().getUserId().equals(currentUser.getUserId());
        boolean isProjectMember = projectMembersRepo.existsByProjIdAndUserId(task.getProjId(), currentUser);
        boolean isManager = currentUser.getRoles().stream().anyMatch(r -> r.getRole().name().equals("MANAGER"))
                && (isOwner || isProjectMember);

        if (!isAdmin && !isOwner && !isCreator && !isManager) {
            throw new UnauthorizedActionException("You are not authorized to update this task's due date.");
        }

        Instant oldDueDate = task.getDueDate();
        task.setDueDate(request.getDueDate());
        Task updated = taskRepo.save(task);
        taskActivityService.logActivity(
                updated,
                currentUser,
                updated.getProjId(),
                ActionTypeEnum.DUE_DATE_CHANGED,
                oldDueDate != null ? oldDueDate.toString() : null,
                request.getDueDate().toString()
        );

        return mapToResponse(updated);
    }


    @Transactional
    public void deleteTask(Long taskId) {
        if (!taskRepo.existsById(taskId)) {
            throw new RuntimeException("Task not found with id: " + taskId);
        }
        taskRepo.deleteById(taskId);
    }

    private TaskResponse mapToResponse(Task task) {
        List<TaskAssignment> assignments = assignementRepo.findByTaskId(task);
        TaskAssignment latestAssignment = assignments.isEmpty() ? null : assignments.get(assignments.size() - 1);
        return TaskResponse.builder()
                .taskId(task.getTaskId())
                .projectId(task.getProjId().getProjectId())
                .projectName(task.getProjId().getName())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .estimatedEffortHours(task.getEstimatedEffortHours())
                .createdByUsername(task.getCreatedBy() != null ? task.getCreatedBy().getUserName() : null)
                .assigneeUserId(latestAssignment != null ? latestAssignment.getUserId().getUserId() : null)
                .asigneeUsername(latestAssignment != null ? latestAssignment.getUserId().getUserName() : null)
                .asigneeName(latestAssignment != null ? latestAssignment.getUserId().getName() : null)
                .startDate(task.getStartDate())
                .dueDate(task.getDueDate())
                .completionDate(task.getCompletion_date())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

}
