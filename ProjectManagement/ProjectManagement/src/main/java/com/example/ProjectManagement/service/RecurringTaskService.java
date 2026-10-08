package com.example.ProjectManagement.service;

import com.example.ProjectManagement.DTO.Request.RecurringRuleRequest;
import com.example.ProjectManagement.DTO.Response.RecurringRuleResponse;
import com.example.ProjectManagement.exception.ResourceNotFoundException;
import com.example.ProjectManagement.model.Entity.RecurringTaskRules;
import com.example.ProjectManagement.model.Entity.Task;
import com.example.ProjectManagement.model.Enum.ActionTypeEnum;
import com.example.ProjectManagement.model.Enum.TaskStatus;
import com.example.ProjectManagement.repository.RecurringTaskRepo;
import com.example.ProjectManagement.repository.TaskRepo;
import com.example.ProjectManagement.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecurringTaskService {
    private final RecurringTaskRepo recurringTaskRepo;
    private final TaskRepo taskRepo;
    private final UserRepo userRepo;
    private final TaskActivityService activityService;

    @Transactional
    public RecurringRuleResponse createOrUpdateRule(Long taskID, RecurringRuleRequest request, String Username){
        Task task = taskRepo.findById(taskID)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskID));
        RecurringTaskRules rule = recurringTaskRepo.findByTask_TaskId(taskID)
                .orElse(new RecurringTaskRules());
        rule.setTask(task);
        rule.setFrequency(request.getFrequency().toUpperCase());
        rule.setIntervalValue(Math.max(1, request.getIntervalValue()));
        rule.setDayOfWeek(request.getDayOfWeek());
        rule.setDayOfMonth(request.getDayOfMonth());
        rule.setStartDate(request.getStartDate());
        rule.setEndDate(request.getEndDate());
        rule.setActive(true);

        Instant initialRun = calculateNextRun(request.getFrequency().toUpperCase(), rule.getIntervalValue(), request.getStartDate());
        rule.setNextRunAt(initialRun);
        task.setRecurring(true);
        taskRepo.save(task);
        RecurringTaskRules saved = recurringTaskRepo.save(rule);
        return mapToResponse(saved);
    }
    public RecurringRuleResponse getRuleForTask(Long taskId) {
        RecurringTaskRules rule = recurringTaskRepo.findByTask_TaskId(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("No recurring rule found for task ID: " + taskId));
        return mapToResponse(rule);
    }
    @Transactional
    public void deactivateRule(Long taskId) {
        RecurringTaskRules rule = recurringTaskRepo.findByTask_TaskId(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("No recurring rule found for task ID: " + taskId));

        rule.setActive(false);
        recurringTaskRepo.save(rule);
        Task task = rule.getTask();
        if (task != null) {
            task.setRecurring(false);
            taskRepo.save(task);
        }
    }

    @Transactional
    public void executeDueRecurringRules() {
        Instant now = Instant.now();
        List<RecurringTaskRules> dueRules = recurringTaskRepo.findByIsActiveTrueAndNextRunAtLessThanEqual(now);
        for (RecurringTaskRules rule : dueRules) {

            if (rule.getEndDate() != null && now.isAfter(rule.getEndDate())) {
                rule.setActive(false);
                recurringTaskRepo.save(rule);
                continue;
            }
            Task templateTask = rule.getTask();
            if (templateTask == null) continue;

            Task newTask = new Task();
            newTask.setProjId(templateTask.getProjId());
            newTask.setTitle(templateTask.getTitle() + " (Recurring)");
            newTask.setDescription(templateTask.getDescription());
            newTask.setStatus(TaskStatus.ASSIGNED);
            newTask.setPriority(templateTask.getPriority());
            newTask.setCreatedBy(templateTask.getCreatedBy());
            newTask.setEstimatedEffortHours(templateTask.getEstimatedEffortHours());
            newTask.setStartDate(now);


            long durationDays = (templateTask.getStartDate() != null && templateTask.getDueDate() != null)
                    ? ChronoUnit.DAYS.between(templateTask.getStartDate(), templateTask.getDueDate())
                    : 7;
            newTask.setDueDate(now.plus(Math.max(1, durationDays), ChronoUnit.DAYS));
            newTask.setRecurring(false);
            Task savedTask = taskRepo.save(newTask);

            activityService.logActivity(
                    savedTask,
                    templateTask.getCreatedBy(),
                    savedTask.getProjId(),
                    ActionTypeEnum.TASK_CREATED,
                    null,
                    "Auto-created from recurring schedule"
            );

            Instant nextRun = calculateNextRun(rule.getFrequency(), rule.getIntervalValue(), rule.getNextRunAt());
            rule.setNextRunAt(nextRun);
            recurringTaskRepo.save(rule);
        }
    }
    private Instant calculateNextRun(String frequency, int interval, Instant baseDate) {
        Instant base = (baseDate != null) ? baseDate : Instant.now();
        return switch (frequency) {
            case "DAILY" -> base.plus(interval, ChronoUnit.DAYS);
            case "WEEKLY" -> base.plus((long) interval * 7, ChronoUnit.DAYS);
            case "MONTHLY" -> base.plus((long) interval * 30, ChronoUnit.DAYS);
            default -> base.plus(7, ChronoUnit.DAYS);
        };
    }

    private RecurringRuleResponse mapToResponse(RecurringTaskRules r) {
        return RecurringRuleResponse.builder()
                .id(r.getRecurringId())
                .taskId(r.getTask() != null ? r.getTask().getTaskId() : null)
                .taskTitle(r.getTask() != null ? r.getTask().getTitle() : null)
                .frequency(r.getFrequency())
                .intervalValue(r.getIntervalValue())
                .dayOfWeek(r.getDayOfWeek())
                .dayOfMonth(r.getDayOfMonth())
                .startDate(r.getStartDate())
                .endDate(r.getEndDate())
                .nextRunAt(r.getNextRunAt())
                .isActive(r.isActive())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
