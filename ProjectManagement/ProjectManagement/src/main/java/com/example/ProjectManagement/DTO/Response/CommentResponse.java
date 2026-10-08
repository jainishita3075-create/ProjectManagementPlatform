package com.example.ProjectManagement.DTO.Response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CommentResponse {
    private Long commentId;
    private Long taskId;
    private String authorUsername;
    private String authorName;
    private String comment;
    private Long parentCommentId;
    private Instant createdAt;
    private Instant updatedAt;
}
