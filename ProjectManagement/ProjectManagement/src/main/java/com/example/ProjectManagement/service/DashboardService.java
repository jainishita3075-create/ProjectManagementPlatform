package com.example.ProjectManagement.service;

import com.example.ProjectManagement.DTO.Response.DashboardSummaryResponse;
import com.example.ProjectManagement.DTO.Response.ProjectProgressResponse;
import com.example.ProjectManagement.DTO.Response.UserWorkloadResponse;
import com.example.ProjectManagement.exception.ResourceNotFoundException;
import com.example.ProjectManagement.exception.UnauthorizedActionException;
import com.example.ProjectManagement.model.Entity.Project;
import com.example.ProjectManagement.model.Entity.Task;
import com.example.ProjectManagement.model.Entity.TaskAssignment;
import com.example.ProjectManagement.model.Entity.Users;
import com.example.ProjectManagement.model.Enum.ProjectStatus;
import com.example.ProjectManagement.model.Enum.TaskStatus;
import com.example.ProjectManagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProjectRepo projectRepo;
    private final TaskRepo taskRepo;
    private final UserRepo userRepo;
    private final ProjectMembersRepo projectMembersRepo;
    private final TaskAssignementRepo taskAssignementRepo;

    public DashboardSummaryResponse getDashboardSummary(String username) {
        Users currentUser = userRepo.findByUserName(username);
        if (currentUser == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getRole().name().equals("ADMIN"));
        List<Project> userProjects;
        if (isAdmin) {
            userProjects = projectRepo.findAll();
        } else {
            Set<Long> projectIds = new HashSet<>();
            List<Project> owned = projectRepo.findByOwner(currentUser);
            owned.forEach(p -> projectIds.add(p.getProjectId()));
            projectMembersRepo.findByUserId(currentUser)
                    .forEach(pm -> projectIds.add(pm.getProjId().getProjectId()));

            userProjects = projectRepo.findAllById(projectIds);
        }

        long totalProjects = userProjects.size();
        long activeProjects = userProjects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.ACTIVE)
                .count();
        long completedProjects = userProjects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.COMPLETED)
                .count();
        List<Task> allTasks = new ArrayList<>();
        for (Project project : userProjects) {
            allTasks.addAll(taskRepo.findByProjId(project));
        }

        Instant now = Instant.now();
        long totalTasks = allTasks.size();
        long completedTasks = allTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                .count();
        long inProgressTasks = allTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS)
                .count();
        long overdueTasks = allTasks.stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED
                        && t.getStatus() != TaskStatus.CANCELLED
                        && t.getDueDate() != null
                        && t.getDueDate().isBefore(now))
                .count();
        List<UserWorkloadResponse> workloadByUser = computeWorkloadByUser(allTasks, now);

        return DashboardSummaryResponse.builder()
                .totalProjects(totalProjects)
                .activeProjects(activeProjects)
                .completedProjects(completedProjects)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .inProgressTasks(inProgressTasks)
                .overdueTasks(overdueTasks)
                .workloadByUser(workloadByUser)
                .build();
    }

    public ProjectProgressResponse getProjectProgress(Long projectId, String username) {
        Users currentUser = userRepo.findByUserName(username);
        if (currentUser == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getRole().name().equals("ADMIN"));
        boolean isOwner = project.getOwner() != null && project.getOwner().getUserId().equals(currentUser.getUserId());
        boolean isMember = projectMembersRepo.existsByProjIdAndUserId(project, currentUser);

        if (!isAdmin && !isOwner && !isMember) {
            throw new UnauthorizedActionException("You are not authorized to view progress for this project.");
        }

        List<Task> tasks = taskRepo.findByProjId(project);
        Instant now = Instant.now();

        long totalTasks = tasks.size();
        long completedTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        long inProgressTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long pendingTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.ASSIGNED || t.getStatus() == TaskStatus.BLOCKED).count();
        long overdueTasks = tasks.stream().filter(t -> t.getStatus() != TaskStatus.COMPLETED
                && t.getStatus() != TaskStatus.CANCELLED
                && t.getDueDate() != null
                && t.getDueDate().isBefore(now)).count();

        double progressPercentage = totalTasks == 0 ? 0.0 :
                BigDecimal.valueOf(((double) completedTasks / totalTasks) * 100.0)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();

        BigDecimal totalEstimatedHours = tasks.stream()
                .map(t -> t.getEstimatedEffortHours() != null ? t.getEstimatedEffortHours() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ProjectProgressResponse.builder()
                .projectId(project.getProjectId())
                .projectName(project.getName())
                .status(project.getStatus())
                .priority(project.getPriority())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .inProgressTasks(inProgressTasks)
                .pendingTasks(pendingTasks)
                .overdueTasks(overdueTasks)
                .progressPercentage(progressPercentage)
                .totalEstimatedHours(totalEstimatedHours)
                .build();
    }

    private List<UserWorkloadResponse> computeWorkloadByUser(List<Task> tasks, Instant now) {
        Map<Long, UserWorkloadBuilder> userStatsMap = new HashMap<>();

        for (Task task : tasks) {
            List<TaskAssignment> assignments = taskAssignementRepo.findByTaskId(task);
            for (TaskAssignment assignment : assignments) {
                Users assignee = assignment.getUserId();
                if (assignee == null) continue;

                UserWorkloadBuilder builder = userStatsMap.computeIfAbsent(assignee.getUserId(), id -> new UserWorkloadBuilder(assignee));
                builder.totalTasks++;

                if (task.getStatus() == TaskStatus.COMPLETED) {
                    builder.completedTasks++;
                } else {
                    builder.pendingTasks++;
                    if (task.getDueDate() != null && task.getDueDate().isBefore(now)) {
                        builder.overdueTasks++;
                    }
                }
            }
        }

        return userStatsMap.values().stream()
                .map(UserWorkloadBuilder::build)
                .collect(Collectors.toList());
    }

    private static class UserWorkloadBuilder {
        private final Users user;
        long totalTasks = 0;
        long completedTasks = 0;
        long pendingTasks = 0;
        long overdueTasks = 0;

        UserWorkloadBuilder(Users user) {
            this.user = user;
        }

        UserWorkloadResponse build() {
            return UserWorkloadResponse.builder()
                    .userId(user.getUserId())
                    .username(user.getUserName())
                    .name(user.getName())
                    .totalAssignedTasks(totalTasks)
                    .completedTasks(completedTasks)
                    .pendingTasks(pendingTasks)
                    .overdueTasks(overdueTasks)
                    .build();
        }
    }
}
