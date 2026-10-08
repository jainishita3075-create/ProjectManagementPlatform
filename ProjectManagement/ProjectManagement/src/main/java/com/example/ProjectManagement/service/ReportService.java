package com.example.ProjectManagement.service;

import com.example.ProjectManagement.DTO.Response.ProjectProgressReportResponse;
import com.example.ProjectManagement.DTO.Response.ProjectReportItem;
import com.example.ProjectManagement.DTO.Response.UserPerformanceItem;
import com.example.ProjectManagement.DTO.Response.UserPerformanceReportResponse;
import com.example.ProjectManagement.exception.BadRequestException;
import com.example.ProjectManagement.model.Entity.Project;
import com.example.ProjectManagement.model.Entity.Task;
import com.example.ProjectManagement.model.Entity.TaskAssignment;
import com.example.ProjectManagement.model.Entity.Users;
import com.example.ProjectManagement.model.Enum.TaskStatus;
import com.example.ProjectManagement.repository.ProjectMembersRepo;
import com.example.ProjectManagement.repository.ProjectRepo;
import com.example.ProjectManagement.repository.TaskAssignementRepo;
import com.example.ProjectManagement.repository.TaskRepo;
import com.example.ProjectManagement.repository.UserRepo;
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
public class ReportService {

    private final ProjectRepo projectRepo;
    private final TaskRepo taskRepo;
    private final UserRepo userRepo;
    private final ProjectMembersRepo projectMembersRepo;
    private final TaskAssignementRepo taskAssignementRepo;

    public ProjectProgressReportResponse generateProjectProgressReport(Instant from, Instant to, String username) {
        validateDateRange(from, to);

        Users currentUser = userRepo.findByUserName(username);
        if (currentUser == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getRole().name().equals("ADMIN"));

        List<Project> scopeProjects;
        if (isAdmin) {
            scopeProjects = projectRepo.findAll();
        } else {
            Set<Long> projectIds = new HashSet<>();
            projectRepo.findByOwner(currentUser).forEach(p -> projectIds.add(p.getProjectId()));
            projectMembersRepo.findByUserId(currentUser).forEach(pm -> projectIds.add(pm.getProjId().getProjectId()));
            scopeProjects = projectRepo.findAllById(projectIds);
        }

        long totalCreatedTasksInPeriod = 0;
        long totalCompletedTasksInPeriod = 0;
        long totalOverdueInPeriod = 0;
        List<ProjectReportItem> projectBreakdown = new ArrayList<>();

        for (Project project : scopeProjects) {
            List<Task> projectTasks = taskRepo.findByProjId(project);

            List<Task> periodCreated = projectTasks.stream()
                    .filter(t -> t.getCreatedAt() != null && !t.getCreatedAt().isBefore(from) && !t.getCreatedAt().isAfter(to))
                    .collect(Collectors.toList());

            long periodCompleted = projectTasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.COMPLETED && t.getCompletion_date() != null
                            && !t.getCompletion_date().isBefore(from) && !t.getCompletion_date().isAfter(to))
                    .count();

            long periodOverdue = projectTasks.stream()
                    .filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getStatus() != TaskStatus.CANCELLED
                            && t.getDueDate() != null && t.getDueDate().isBefore(to))
                    .count();

            totalCreatedTasksInPeriod += periodCreated.size();
            totalCompletedTasksInPeriod += periodCompleted;
            totalOverdueInPeriod += periodOverdue;

            long totalProjectTasks = projectTasks.size();
            long totalCompletedProjectTasks = projectTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();

            double progress = totalProjectTasks == 0 ? 0.0 :
                    BigDecimal.valueOf(((double) totalCompletedProjectTasks / totalProjectTasks) * 100.0)
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();

            projectBreakdown.add(ProjectReportItem.builder()
                    .projectId(project.getProjectId())
                    .projectName(project.getName())
                    .status(project.getStatus().name())
                    .totalTasks(totalProjectTasks)
                    .completedTasks(totalCompletedProjectTasks)
                    .progressPercentage(progress)
                    .build());
        }

        double overallRate = totalCreatedTasksInPeriod == 0 ? 0.0 :
                BigDecimal.valueOf(((double) totalCompletedTasksInPeriod / totalCreatedTasksInPeriod) * 100.0)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();

        return ProjectProgressReportResponse.builder()
                .fromDate(from)
                .toDate(to)
                .totalProjectsInScope(scopeProjects.size())
                .tasksCreatedInPeriod(totalCreatedTasksInPeriod)
                .tasksCompletedInPeriod(totalCompletedTasksInPeriod)
                .overdueTasksInPeriod(totalOverdueInPeriod)
                .overallCompletionRate(overallRate)
                .projectBreakdown(projectBreakdown)
                .build();
    }

    public UserPerformanceReportResponse generateUserPerformanceReport(Instant from, Instant to) {
        validateDateRange(from, to);

        List<Users> users = userRepo.findAll();
        List<UserPerformanceItem> result = new ArrayList<>();

        for (Users user : users) {
            List<TaskAssignment> assignments = taskAssignementRepo.findByUserId(user);

            long assignedInPeriod = assignments.stream()
                    .filter(a -> a.getAssignedAt() != null && !a.getAssignedAt().isBefore(from) && !a.getAssignedAt().isAfter(to))
                    .count();

            long completedInPeriod = assignments.stream()
                    .map(TaskAssignment::getTaskId)
                    .filter(Objects::nonNull)
                    .filter(t -> t.getStatus() == TaskStatus.COMPLETED && t.getCompletion_date() != null
                            && !t.getCompletion_date().isBefore(from) && !t.getCompletion_date().isAfter(to))
                    .count();

            long overdue = assignments.stream()
                    .map(TaskAssignment::getTaskId)
                    .filter(Objects::nonNull)
                    .filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getStatus() != TaskStatus.CANCELLED
                            && t.getDueDate() != null && t.getDueDate().isBefore(to))
                    .count();

            double rate = assignedInPeriod == 0 ? 0.0 :
                    BigDecimal.valueOf(((double) completedInPeriod / assignedInPeriod) * 100.0)
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();

            result.add(UserPerformanceItem.builder()
                    .userId(user.getUserId())
                    .username(user.getUserName())
                    .name(user.getName())
                    .tasksAssigned(assignedInPeriod)
                    .tasksCompleted(completedInPeriod)
                    .tasksOverdue(overdue)
                    .completionRate(rate)
                    .build());
        }

        return UserPerformanceReportResponse.builder()
                .fromDate(from)
                .toDate(to)
                .userPerformanceList(result)
                .build();
    }

    private void validateDateRange(Instant from, Instant to) {
        if (from == null || to == null) {
            throw new BadRequestException("Both 'from' and 'to' dates are required.");
        }
        if (from.isAfter(to)) {
            throw new BadRequestException("'from' date cannot be after 'to' date.");
        }
    }
}
