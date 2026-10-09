package com.example.ProjectManagement.service;

import com.example.ProjectManagement.DTO.Request.AssignTaskRequest;
import com.example.ProjectManagement.DTO.Request.RespondAssignmentRequest;
import com.example.ProjectManagement.DTO.Response.TaskAssignmentResponse;
import com.example.ProjectManagement.model.Entity.Task;
import com.example.ProjectManagement.model.Entity.TaskAssignment;
import com.example.ProjectManagement.model.Entity.Users;
import com.example.ProjectManagement.model.Enum.ActionTypeEnum;
import com.example.ProjectManagement.model.Enum.AssignmentStatus;
import com.example.ProjectManagement.model.Enum.NotificationTypeEnum;
import com.example.ProjectManagement.model.Entity.ProjectMembers;
import com.example.ProjectManagement.model.Enum.ProjectMemberRole;
import com.example.ProjectManagement.repository.ProjectMembersRepo;
import com.example.ProjectManagement.repository.TaskAssignementRepo;
import com.example.ProjectManagement.repository.TaskRepo;
import com.example.ProjectManagement.repository.UserRepo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskAssignmentService {

    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TaskAssignementRepo assignementRepo;

    @Autowired
    private ProjectMembersRepo projectMembersRepo;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TaskActivityService taskActivityService;

    @Transactional
    @CacheEvict(value = {"projectTasks", "projects", "projectById", "projectMembers"}, allEntries = true)
    public TaskAssignmentResponse assignTask(AssignTaskRequest request, String assignedByUsername){
        Users assignedBy = userRepo.findByUserName(assignedByUsername);
        if (assignedBy == null){
            throw new UsernameNotFoundException("Assigning User not found: " + assignedByUsername);
        }
        Users assignee = userRepo.findByUserName(request.getAssignedUsername());
        if (assignee == null){
            throw new UsernameNotFoundException("Assignee not found: " + request.getAssignedUsername());
        }
        Task task = taskRepo.findById(request.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + request.getTaskId()));
        if (assignementRepo.existsByTaskIdAndUserId(task, assignee)) {
            throw new RuntimeException("Task is already assigned to user: " + request.getAssignedUsername());
        }
        List<TaskAssignment> previousAssignments = assignementRepo.findByTaskId(task);
        String previousAssignee = previousAssignments.isEmpty()
                ? "UNASSIGNED"
                : previousAssignments.get(previousAssignments.size() - 1).getUserId().getUserName();

        TaskAssignment assignment = new TaskAssignment();
        assignment.setTaskId(task);
        assignment.setUserId(assignee);
        assignment.setAssignedBy(assignedBy);
        assignment.setStatus(AssignmentStatus.PENDING);
        assignment.setAssignedAt(Instant.now());
        assignment.setDescription(request.getDescription());
        TaskAssignment saved = assignementRepo.save(assignment);

        // Ensure assignee is also added as a project member so the project & task appear in their workspace
        if (task.getProjId() != null && !projectMembersRepo.existsByProjIdAndUserId(task.getProjId(), assignee)) {
            ProjectMembers pm = new ProjectMembers();
            pm.setProjId(task.getProjId());
            pm.setUserId(assignee);
            pm.setMemberRole(ProjectMemberRole.EDITOR);
            pm.setJoinedAt(Instant.now());
            projectMembersRepo.save(pm);
        }

        notificationService.sendNotification(
                assignee,
                assignedBy,
                task,
                task.getProjId(),
                NotificationTypeEnum.TASK_ASSIGNED,
                "New Task Assigned",
                "You have been assigned to task: " + task.getTitle()
        );
        taskActivityService.logActivity(
                task,
                assignedBy,
                task.getProjId(),
                ActionTypeEnum.ASSIGNMENT_UPDATED,
                previousAssignee,
                assignee.getUserName()
        );
        return mapToResponse(saved);
    }


    public List<TaskAssignmentResponse> getMyAssignments(String username) {
        Users user = userRepo.findByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return assignementRepo.findByUserId(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TaskAssignmentResponse> getAssignmentsByTaskId(Long taskId) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        return assignementRepo.findByTaskId(task).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TaskAssignmentResponse respondToAssignment(Long assignmentId, RespondAssignmentRequest request, String username) {
        TaskAssignment assignment = assignementRepo.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));
        if (!assignment.getUserId().getUserName().equals(username)) {
            throw new RuntimeException("You are not authorized to respond to this assignment.");
        }
        assignment.setStatus(request.getStatus());
        assignment.setResponseNote(request.getResponseNote());
        assignment.setRespondedAt(Instant.now());
        TaskAssignment updated = assignementRepo.save(assignment);
        return mapToResponse(updated);
    }

    private TaskAssignmentResponse mapToResponse(TaskAssignment assignment) {
        return TaskAssignmentResponse.builder()
                .assignmentId(assignment.getTaskAssignmentId())
                .taskId(assignment.getTaskId().getTaskId())
                .taskTitle(assignment.getTaskId().getTitle())
                .assignedToUsername(assignment.getUserId().getUserName())
                .assignedByUsername(assignment.getAssignedBy().getUserName())
                .status(assignment.getStatus())
                .assignedAt(assignment.getAssignedAt())
                .respondedAt(assignment.getRespondedAt())
                .responseNote(assignment.getResponseNote())
                .description(assignment.getDescription())
                .build();
    }
}
