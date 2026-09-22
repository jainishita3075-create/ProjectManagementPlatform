package com.example.ProjectManagement.model.Entity;


import com.example.ProjectManagement.model.Enum.ActionTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Data
@Entity
@Table(name = "task_activity")
@AllArgsConstructor
@NoArgsConstructor
public class TaskActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long taId;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task taskId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users userId;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project proj_id;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionTypeEnum actionType;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    @CreationTimestamp
    @Column(name = "creation_time", nullable = false, updatable = false)
    private Instant creationTime;
}
