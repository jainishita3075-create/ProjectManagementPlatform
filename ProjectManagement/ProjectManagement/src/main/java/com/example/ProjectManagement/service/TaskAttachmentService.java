package com.example.ProjectManagement.service;

import com.example.ProjectManagement.DTO.Response.TaskAttachmentResponse;
import com.example.ProjectManagement.exception.BadRequestException;
import com.example.ProjectManagement.exception.ResourceNotFoundException;
import com.example.ProjectManagement.exception.UnauthorizedActionException;
import com.example.ProjectManagement.model.Entity.Task;
import com.example.ProjectManagement.model.Entity.TaskAttachments;
import com.example.ProjectManagement.model.Entity.Users;
import com.example.ProjectManagement.model.Enum.FileTypeEnum;
import com.example.ProjectManagement.repository.TaskAttachmentRepo;
import com.example.ProjectManagement.repository.TaskRepo;
import com.example.ProjectManagement.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskAttachmentService {

    private final TaskAttachmentRepo attachmentRepo;

    private final TaskRepo taskRepo;

    private final UserRepo userRepo;

    private final Path fileStorageLocation = Paths.get("uploads/attachments").toAbsolutePath().normalize();
    @Transactional
    public TaskAttachmentResponse uploadAttachment(Long taskId, MultipartFile file, String currentUsername) {
        if (file.isEmpty()) {
            throw new BadRequestException("Cannot upload an empty file");
        }

        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        Users user = userRepo.findByUserName(currentUsername);
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + currentUsername);
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        String fileExtension = getFileExtension(originalFileName);
        FileTypeEnum detectedType = detectFileType(fileExtension);

        try {
            Files.createDirectories(this.fileStorageLocation);
            String storedFileName = UUID.randomUUID() + "_" + originalFileName;
            Path targetLocation = this.fileStorageLocation.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            TaskAttachments attachment = new TaskAttachments();
            attachment.setTaskId(task);
            attachment.setUploadedBy(user);
            attachment.setFileName(originalFileName);
            attachment.setUrl(storedFileName);
            attachment.setFileType(detectedType);
            attachment.setFileSize(file.getSize());
            attachment.setUploadedAt(Instant.now());
            TaskAttachments saved = attachmentRepo.save(attachment);
            return mapToResponse(saved);
        } catch (IOException ex) {
            throw new BadRequestException("Could not store file. Please try again: " + ex.getMessage());
        }
    }

    public List<TaskAttachmentResponse> getAttachmentsForTask(Long taskId) {
        if (!taskRepo.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found with ID: " + taskId);
        }
        return attachmentRepo.findByTaskId_TaskIdOrderByUploadedAtDesc(taskId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Resource loadFileAsResource(Long attachmentId) {
        TaskAttachments attachment = attachmentRepo.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with ID: " + attachmentId));
        try {
            Path filePath = this.fileStorageLocation.resolve(attachment.getUrl()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found on disk for attachment ID: " + attachmentId);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File path is invalid for attachment ID: " + attachmentId);
        }
    }

    public TaskAttachments getAttachmentEntity(Long attachmentId) {
        return attachmentRepo.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with ID: " + attachmentId));
    }

    @Transactional
    public void deleteAttachment(Long attachmentId, String currentUsername) {
        TaskAttachments attachment = attachmentRepo.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with ID: " + attachmentId));

        Users currentUser = userRepo.findByUserName(currentUsername);
        if (currentUser == null) {
            throw new ResourceNotFoundException("User not found: " + currentUsername);
        }

        boolean isUploader = attachment.getUploadedBy().getUserId().equals(currentUser.getUserId());
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getRole().name().equals("ADMIN"));
        if (!isUploader && !isAdmin) {
            throw new UnauthorizedActionException("Only the uploader or an Admin can delete this attachment.");
        }
        try {
            Path filePath = this.fileStorageLocation.resolve(attachment.getUrl()).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {}
        attachmentRepo.delete(attachment);
    }

    private FileTypeEnum detectFileType(String ext) {
        if (ext == null) return FileTypeEnum.OTHER;
        return switch (ext.toLowerCase()) {
            case "csv" -> FileTypeEnum.CSV;
            case "xls", "xlsx" -> FileTypeEnum.EXCEL;
            case "mp4", "mkv", "avi", "mov" -> FileTypeEnum.VIDEO;
            case "pdf", "doc", "docx", "txt" -> FileTypeEnum.DOCS;
            case "jpg", "jpeg", "png", "gif", "webp" -> FileTypeEnum.IMAGES;
            default -> FileTypeEnum.OTHER;
        };
    }

    private String getFileExtension(String filename) {
        int lastIndex = filename.lastIndexOf('.');
        return (lastIndex == -1) ? "" : filename.substring(lastIndex + 1);
    }

    private TaskAttachmentResponse mapToResponse(TaskAttachments att) {
        return TaskAttachmentResponse.builder()
                .id(att.getTaskAttachmentId())
                .taskId(att.getTaskId().getTaskId())
                .fileName(att.getFileName())
                .downloadUrl("/api/tasks/attachments/" + att.getTaskAttachmentId() + "/download")
                .fileType(att.getFileType())
                .fileSize(att.getFileSize())
                .uploadedByUserId(att.getUploadedBy().getUserId())
                .uploadedByUsername(att.getUploadedBy().getUserName())
                .uploadedAt(att.getUploadedAt())
                .build();
    }

}
