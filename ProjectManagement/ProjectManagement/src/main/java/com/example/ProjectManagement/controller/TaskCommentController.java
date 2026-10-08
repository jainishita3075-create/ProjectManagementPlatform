package com.example.ProjectManagement.controller;

import com.example.ProjectManagement.DTO.Request.CreateCommentRequest;
import com.example.ProjectManagement.DTO.Response.ApiResponse;
import com.example.ProjectManagement.DTO.Response.CommentResponse;
import com.example.ProjectManagement.service.TaskCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskCommentController {

    @Autowired
    private TaskCommentService commentService;

    @PostMapping("/{taskId}/comments")
    public ApiResponse<CommentResponse> addComment(
            @PathVariable Long taskId,
            @RequestBody CreateCommentRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        CommentResponse response = commentService.addComment(taskId, request, username);
        return ApiResponse.success("Comment added successfully", response);
    }

    @GetMapping("/{taskId}/comments")
    public ApiResponse<List<CommentResponse>> getComments(@PathVariable Long taskId) {
        List<CommentResponse> comments = commentService.getCommentsForTask(taskId);
        return ApiResponse.success("Comments retrieved successfully", comments);
    }

    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {
        String username = authentication.getName();
        commentService.deleteComment(commentId, username);
        return ApiResponse.success("Comment deleted successfully", null);
    }
}

