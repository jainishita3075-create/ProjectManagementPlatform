package com.example.ProjectManagement.controller;

import com.example.ProjectManagement.DTO.Response.ApiResponse;
import com.example.ProjectManagement.DTO.Response.TaskAttachmentResponse;
import com.example.ProjectManagement.model.Entity.TaskAttachments;
import com.example.ProjectManagement.service.TaskAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskAttachmentController {

    private final TaskAttachmentService attachmentService;

    @PostMapping(value = "/tasks/{taskId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TaskAttachmentResponse>> uploadAttachment(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        TaskAttachmentResponse response = attachmentService.uploadAttachment(taskId, file, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("File uploaded successfully", response));
    }

    @GetMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<ApiResponse<List<TaskAttachmentResponse>>> getTaskAttachments(@PathVariable Long taskId) {
        List<TaskAttachmentResponse> attachments = attachmentService.getAttachmentsForTask(taskId);
        return ResponseEntity.ok(ApiResponse.success("Task attachments fetched successfully", attachments));
    }

    @GetMapping("/tasks/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
        Resource resource = attachmentService.loadFileAsResource(attachmentId);
        TaskAttachments attachment = attachmentService.getAttachmentEntity(attachmentId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/tasks/attachments/{attachmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @PathVariable Long attachmentId,
            Authentication authentication) {
        attachmentService.deleteAttachment(attachmentId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Attachment deleted successfully", null));
    }
}
