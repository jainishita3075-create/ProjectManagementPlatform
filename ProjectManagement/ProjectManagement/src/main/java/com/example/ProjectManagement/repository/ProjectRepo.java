package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.Project;
import com.example.ProjectManagement.model.Entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepo extends JpaRepository<Project, Long> {
    Optional<Project> findByProjectId(Long projectId);
    List<Project> findByOwner(Users owner);
    Page<Project> findByOwner(Users owner, Pageable pageable);
    List<Project> findByCreatedAtBetween(Instant from, Instant to);

    @Query("SELECT DISTINCT p FROM Project p " +
           "LEFT JOIN ProjectMembers pm ON pm.projId = p " +
           "WHERE p.owner = :user " +
           "OR pm.userId = :user " +
           "OR p.projectId IN (SELECT t.projId.projectId FROM TaskAssignment ta JOIN ta.taskId t WHERE ta.userId = :user) " +
           "OR p.projectId IN (SELECT t.projId.projectId FROM Task t WHERE t.createdBy = :user)")
    Page<Project> findAccessibleProjects(@Param("user") Users user, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Project p " +
           "LEFT JOIN ProjectMembers pm ON pm.projId = p " +
           "WHERE p.owner = :user " +
           "OR pm.userId = :user " +
           "OR p.projectId IN (SELECT t.projId.projectId FROM TaskAssignment ta JOIN ta.taskId t WHERE ta.userId = :user) " +
           "OR p.projectId IN (SELECT t.projId.projectId FROM Task t WHERE t.createdBy = :user)")
    List<Project> findAccessibleProjects(@Param("user") Users user);
}
