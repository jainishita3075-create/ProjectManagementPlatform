package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.RecurringTaskRules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringTaskRepo extends JpaRepository <RecurringTaskRules, Long> {
    RecurringTaskRules findByRecurringId(Long recurringId);
    Optional<RecurringTaskRules> findByTask_TaskId(Long taskId);
    boolean existsByTask_TaskId(Long taskId);
    List<RecurringTaskRules> findByIsActiveTrueAndNextRunAtLessThanEqual(Instant now);
}
