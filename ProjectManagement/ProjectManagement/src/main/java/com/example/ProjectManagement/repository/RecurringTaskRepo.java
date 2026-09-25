package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.RecurringTaskRules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecurringTaskRepo extends JpaRepository <RecurringTaskRules, Long> {
    RecurringTaskRules findByRecurringId(Long recurringId);
}
