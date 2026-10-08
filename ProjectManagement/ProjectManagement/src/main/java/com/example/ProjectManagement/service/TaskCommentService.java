package com.example.ProjectManagement.service;

import com.example.ProjectManagement.DTO.Request.CreateCommentRequest;
import com.example.ProjectManagement.DTO.Response.CommentResponse;
import com.example.ProjectManagement.exception.ResourceNotFoundException;
import com.example.ProjectManagement.exception.UnauthorizedActionException;
import com.example.ProjectManagement.model.Entity.Task;
import com.example.ProjectManagement.model.Entity.TaskComment;
import com.example.ProjectManagement.model.Entity.Users;
import com.example.ProjectManagement.model.Enum.NotificationTypeEnum;
import com.example.ProjectManagement.model.Enum.UserRole;
import com.example.ProjectManagement.repository.TaskCommentRepo;
import com.example.ProjectManagement.repository.TaskRepo;
import com.example.ProjectManagement.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskCommentService {

    @Autowired
    private TaskCommentRepo commentRepo;

    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public CommentResponse addComment(Long taskId, CreateCommentRequest request, String username) {
        Users author = userRepo.findByUserName(username);
        if (author == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        TaskComment comment = new TaskComment();
        comment.setTaskId(task);
        comment.setUserId(author);
        comment.setComment(request.getComment());

        if (request.getParentCommentId() != null) {
            TaskComment parent = commentRepo.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found with id: " + request.getParentCommentId()));
            comment.setParentCommentId(parent);
        }

        TaskComment saved = commentRepo.save(comment);
        if (request.getMentionedUsername() != null && !request.getMentionedUsername().isEmpty()) {
            for (String targetUsername : request.getMentionedUsername()) {
                Users targetUser = userRepo.findByUserName(targetUsername);
                if (targetUser != null && !targetUser.getUserId().equals(author.getUserId())) {
                    notificationService.sendNotification(
                            targetUser,
                            author,
                            task,
                            task.getProjId(),
                            NotificationTypeEnum.COMMENT_MENTION,
                            "New Comment Notification",
                            author.getUserName() + " notified you on task: " + task.getTitle()
                    );
                }
            }
        }

        return mapToResponse(saved);
    }


    public List<CommentResponse> getCommentsForTask(Long taskId) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        return commentRepo.findByTaskIdOrderByCreatedAtAsc(task).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteComment(Long commentId, String currentUsername) {
        TaskComment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
        Users currentUser = userRepo.findByUserName(currentUsername);
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getRole() == UserRole.ADMIN);

        if (!comment.getUserId().getUserName().equals(currentUsername) && !isAdmin) {
            throw new UnauthorizedActionException("You are not authorized to delete this comment.");
        }
        commentRepo.deleteById(commentId);
    }

    private CommentResponse mapToResponse(TaskComment comment) {
        return CommentResponse.builder()
                .commentId(comment.getTcId())
                .taskId(comment.getTaskId().getTaskId())
                .authorUsername(comment.getUserId().getUserName())
                .authorName(comment.getUserId().getName())
                .comment(comment.getComment())
                .parentCommentId(comment.getParentCommentId() != null ? comment.getParentCommentId().getTcId() : null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
