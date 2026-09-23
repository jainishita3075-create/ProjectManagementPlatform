package com.example.ProjectManagement.model.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Data
@Entity
@Table(name = "recurring_task_rule",
indexes = {
        @Index(name = "idx_recc_task_id", columnList = "task_id", unique = true),
        @Index(name = "idx_next_run_at", columnList = "next_run_at"),
        @Index(name = "idx_is_active", columnList = "is_active")
})
@AllArgsConstructor
@NoArgsConstructor
public class RecurringTaskRules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long recurringId;

    @ManyToOne
    @JoinColumn(name = "task_id", unique = true, nullable = false)
    private Task taskId;

    @Column(name = "frequency", nullable = false)
    private String frequency;

    @ColumnDefault(value = "1")
    @Column(name = "interval_value", nullable = false)
    private int intervalValue;

    @Column(name = "day_of_week")
    private int dayOfWeek;

    @Column(name = "day_of_month")
    private int dayOfMonth;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Column(name = "next_run_at")
    private Instant nextRunAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
