package com.example.ProjectManagement.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class CreateCommentRequest {
    @NotBlank(message = "Comment can not be blank")
    private String comment;
    private Long parentCommentId;
    private Set<String> mentionedUsername;
}
