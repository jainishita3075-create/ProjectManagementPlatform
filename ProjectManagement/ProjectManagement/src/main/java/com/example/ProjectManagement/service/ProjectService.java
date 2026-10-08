package com.example.ProjectManagement.service;

import com.example.ProjectManagement.DTO.Request.CreateProjectRequest;
import com.example.ProjectManagement.DTO.Request.UpdateProjectRequest;
import com.example.ProjectManagement.DTO.Response.PagedResponse;
import com.example.ProjectManagement.DTO.Response.ProjectResponse;
import com.example.ProjectManagement.exception.BadRequestException;
import com.example.ProjectManagement.exception.ResourceNotFoundException;
import com.example.ProjectManagement.exception.UnauthorizedActionException;
import com.example.ProjectManagement.model.Entity.Project;
import com.example.ProjectManagement.model.Entity.Task;
import com.example.ProjectManagement.model.Entity.Users;
import com.example.ProjectManagement.model.Enum.ProjectPriority;
import com.example.ProjectManagement.model.Enum.ProjectStatus;
import com.example.ProjectManagement.model.Enum.TaskStatus;
import com.example.ProjectManagement.repository.ProjectMembersRepo;
import com.example.ProjectManagement.repository.ProjectRepo;
import com.example.ProjectManagement.repository.TaskRepo;
import com.example.ProjectManagement.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private ProjectMembersRepo projectMembersRepo;

    public PagedResponse<ProjectResponse> getUserProjectsPaged(String username, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Users user = userRepo.findByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getRole().name().equals("ADMIN"));
        Page<Project> projectPage = isAdmin
                ? projectRepo.findAll(pageable)
                : projectRepo.findAccessibleProjects(user, pageable);

        List<ProjectResponse> content = projectPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PagedResponse.of(projectPage, content);
    }

    public List<ProjectResponse> getUserProjects(String username) {
        Users user = userRepo.findByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getRole().name().equals("ADMIN"));
        List<Project> projects = isAdmin
                ? projectRepo.findAll()
                : projectRepo.findAccessibleProjects(user);

        return projects.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse createProject(CreateProjectRequest request, String username) {
        Users owner = userRepo.findByUserName(username);
        if (owner == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        if (request.getStartDate() != null && request.getDueDate() != null && request.getStartDate().isAfter(request.getDueDate())) {
            throw new BadRequestException("Start date cannot be after the due date.");
        }

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(owner);
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setDueDate(request.getDueDate());
        project.setStatus(request.getStatus() != null ? request.getStatus() : ProjectStatus.ACTIVE);
        project.setPriority(request.getPriority() != null ? request.getPriority() : ProjectPriority.MEDIUM);
        Project saved = projectRepo.save(project);
        return mapToResponse(saved);
    }

    public ProjectResponse getProjectById(Long id, String username) {
        Project project = projectRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        Users currentUser = userRepo.findByUserName(username);
        if (currentUser == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getRole().name().equals("ADMIN"));
        boolean isOwner = project.getOwner() != null && project.getOwner().getUserId().equals(currentUser.getUserId());
        boolean isMember = projectMembersRepo.existsByProjIdAndUserId(project, currentUser);

        if (!isAdmin && !isOwner && !isMember) {
            throw new UnauthorizedActionException("You do not have access to view this project.");
        }

        return mapToResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, UpdateProjectRequest request, String username){
        Project project = projectRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        Users currentUser = userRepo.findByUserName(username);
        if (currentUser == null) {
            throw new ResourceNotFoundException("User not found: " + username);
        }
        boolean isOwner = project.getOwner().getUserId().equals(currentUser.getUserId());
        boolean isElevated = currentUser.getRoles().stream()
                .anyMatch(r -> r.getRole().name().equals("ADMIN") || r.getRole().name().equals("MANAGER"));
        if (!isOwner && !isElevated) {
            throw new UnauthorizedActionException("Only the project owner, managers, or admins can update this project.");
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            project.setEndDate(request.getEndDate());
        }
        if (request.getDueDate() != null) {
            project.setDueDate(request.getDueDate());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            project.setPriority(request.getPriority());
        }
        Project updated = projectRepo.save(project);
        return mapToResponse(updated);
    }

    @Transactional
    public ProjectResponse archiveProject(Long id, String username) {
        Project project = projectRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        Users currentUser = userRepo.findByUserName(username);
        if (currentUser == null) {
            throw new ResourceNotFoundException("User not found: " + username);
        }
        boolean isOwner = project.getOwner().getUserId().equals(currentUser.getUserId());
        boolean isElevated = currentUser.getRoles().stream()
                .anyMatch(r -> r.getRole().name().equals("ADMIN") || r.getRole().name().equals("MANAGER"));
        if (!isOwner && !isElevated) {
            throw new UnauthorizedActionException("Only the project owner, managers, or admins can archive this project.");
        }
        project.setStatus(ProjectStatus.ARCHIVED);
        Project archived = projectRepo.save(project);
        return mapToResponse(archived);
    }

    private ProjectResponse mapToResponse(Project project) {
        List<Task> tasks = taskRepo.findByProjId(project);
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                .count();
        double progressPercentage = totalTasks == 0 ? 0.0 :
                BigDecimal.valueOf(((double) completedTasks / totalTasks) * 100.0)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();

        return ProjectResponse.builder()
                .projectId(project.getProjectId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerUsername(project.getOwner() != null ? project.getOwner().getUserName() : null)
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .dueDate(project.getDueDate())
                .status(project.getStatus())
                .priority(project.getPriority())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .progressPercentage(progressPercentage)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }


}
