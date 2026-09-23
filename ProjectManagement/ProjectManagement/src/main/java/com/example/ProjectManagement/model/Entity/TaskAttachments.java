package com.example.ProjectManagement.model.Entity;


import com.example.ProjectManagement.model.Enum.FileTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.Instant;

@Data
@Entity
@Table(name = "task_attachments",
indexes = {@Index(name = "idx_task_attch_task_id", columnList = "task_id"),
        @Index(name = "idx_uploaded_by", columnList = "uploaded_by")
})
@AllArgsConstructor
@NoArgsConstructor
public class TaskAttachments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long taskAttachmentId;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task taskId;

    @ManyToOne
    @JoinColumn(name = "uploaded_by", nullable = false)
    private Users uploadedBy;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "url", nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileTypeEnum fileType = FileTypeEnum.OTHER;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;
}
