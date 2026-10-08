package com.example.ProjectManagement.service;

import com.example.ProjectManagement.model.Entity.Task;
import com.example.ProjectManagement.model.Enum.ActionTypeEnum;
import com.example.ProjectManagement.model.Enum.NotificationTypeEnum;
import com.example.ProjectManagement.model.Enum.TaskStatus;
import com.example.ProjectManagement.repository.TaskRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskAutomationScheduler {

    private static final Logger log = LoggerFactory.getLogger(TaskAutomationScheduler.class);
    private final RecurringTaskService recurringTaskService;
    private final TaskRepo taskRepo;
    private final TaskActivityService activityService;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0/15 * * * ?")
    public void processRecurringTasksJob() {
        log.info("Running automated recurring tasks execution job...");
        recurringTaskService.executeDueRecurringRules();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void markOverdueTasksJob() {
        log.info("Running automated overdue tasks check...");
        Instant now = Instant.now();
        List<Task> allTasks = taskRepo.findAll();
        for (Task task : allTasks) {
            boolean isPastDue = task.getDueDate() != null && now.isAfter(task.getDueDate());
            boolean isNotCompleted = task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.CANCELLED;
            if (isPastDue && isNotCompleted && task.getStatus() != TaskStatus.OVERDUE) {
                String oldStatus = task.getStatus() != null ? task.getStatus().name() : "N/A";
                task.setStatus(TaskStatus.OVERDUE);
                taskRepo.save(task);

                activityService.logActivity(
                        task,
                        task.getCreatedBy(),
                        task.getProjId(),
                        ActionTypeEnum.STATUS_CHANGED,
                        oldStatus,
                        "OVERDUE (System Auto-Update)"
                );
            }
        }
    }

    @Scheduled(cron = "0 0 8 * * ?")
    public void sendUpcomingDueDateRemindersJob() {
        log.info("Running automated 24-hour due date reminder job...");
        Instant now = Instant.now();
        Instant next24Hours = now.plus(24, ChronoUnit.HOURS);
        List<Task> allTasks = taskRepo.findAll();
        for (Task task : allTasks) {
            if (task.getDueDate() != null &&
                    task.getDueDate().isAfter(now) &&
                    task.getDueDate().isBefore(next24Hours) &&
                    task.getStatus() != TaskStatus.COMPLETED) {
                if (task.getCreatedBy() != null) {
                    notificationService.sendNotification(
                            task.getCreatedBy(),
                            null,
                            task,
                            task.getProjId(),
                            NotificationTypeEnum.REMINDER,
                            "Upcoming Task Deadline",
                            "Reminder: Task '" + task.getTitle() + "' is due within the next 24 hours!"
                    );
                }
            }
        }
    }
}
