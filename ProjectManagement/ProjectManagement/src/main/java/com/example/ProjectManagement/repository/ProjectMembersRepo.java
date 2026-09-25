package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.ProjectMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectMembersRepo extends JpaRepository<ProjectMembers, Long> {
    ProjectMembers findByPmId(Long pmId);
}
