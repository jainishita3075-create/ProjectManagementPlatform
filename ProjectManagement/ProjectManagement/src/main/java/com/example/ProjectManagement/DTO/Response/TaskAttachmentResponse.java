package com.example.ProjectManagement.DTO.Response;

import com.example.ProjectManagement.model.Enum.FileTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskAttachmentResponse {
    private Long id;
    private Long taskId;
    private String fileName;
    private String downloadUrl;
    private FileTypeEnum fileType;
    private Long fileSize;
    private Long uploadedByUserId;
    private String uploadedByUsername;
    private Instant uploadedAt;
}
