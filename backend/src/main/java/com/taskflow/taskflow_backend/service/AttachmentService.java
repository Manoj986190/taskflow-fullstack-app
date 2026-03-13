package com.taskflow.taskflow_backend.service;

import com.taskflow.taskflow_backend.dto.AttachmentListDTO;
import com.taskflow.taskflow_backend.entity.Task;
import com.taskflow.taskflow_backend.entity.TaskAttachment;
import com.taskflow.taskflow_backend.entity.User;
import com.taskflow.taskflow_backend.exception.ResourceNotFoundException;
import com.taskflow.taskflow_backend.exception.TaskAccessDeniedException;
import com.taskflow.taskflow_backend.repository.TaskAttachmentRepository;
import com.taskflow.taskflow_backend.repository.TaskRepository;
import com.taskflow.taskflow_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final TaskAttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // ===============================
    // ALLOWED MIME TYPES (TC-F03)
    // ===============================
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
            "application/zip"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L; // 5MB
    private static final int MAX_FILES_PER_TASK = 5;

    // ===============================
    // UPLOAD (TC-F01, TC-F03, TC-F08)
    // ===============================
    public AttachmentListDTO upload(Long taskId,
                                   MultipartFile file,
                                   String email) throws IOException {

        User uploader = getUserByEmail(email);

        // VIEWER cannot upload (TC-F07)
        if (uploader.getRole().name().equals("VIEWER")) {
            throw new TaskAccessDeniedException(
                    "Viewers are not allowed to upload attachments");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found"));

        // Max 5 files check (TC-F08)
        int existingCount = attachmentRepository.countByTask_Id(taskId);
        if (existingCount >= MAX_FILES_PER_TASK) {
            throw new TaskAccessDeniedException(
                    "Maximum 5 files reached for this task");
        }

        // MIME type check (TC-F03)
        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_TYPES.contains(mimeType)) {
            throw new TaskAccessDeniedException(
                    "File type not allowed");
        }

        // File size server-side check (TC-F02)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new TaskAccessDeniedException(
                    "File exceeds 5 MB limit");
        }

        TaskAttachment attachment = TaskAttachment.builder()
                .task(task)
                .uploader(uploader)
                .originalName(file.getOriginalFilename())
                .mimeType(mimeType)
                .fileSizeBytes(file.getSize())
                .fileData(file.getBytes())
                .build();

        TaskAttachment saved = attachmentRepository.save(attachment);

        return mapToDTO(saved);
    }

    // ===============================
    // LIST — metadata only (TC-F05)
    // ===============================
    public List<AttachmentListDTO> listAttachments(Long taskId) {

        taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found"));

        return attachmentRepository.findByTask_Id(taskId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // ===============================
    // DOWNLOAD (TC-F04)
    // ===============================
    public ResponseEntity<byte[]> download(Long attachmentId) {

        TaskAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Attachment not found"));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                        attachment.getOriginalName() + "\"")
                .body(attachment.getFileData());
    }

    // ===============================
    // DELETE (TC-F06)
    // ===============================
    public void delete(Long attachmentId, String email) {

        User user = getUserByEmail(email);

        TaskAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Attachment not found"));

        boolean isUploader = attachment.getUploader().getId()
                .equals(user.getId());

        boolean isAdminOrManager = user.getRole().name().equals("ADMIN") ||
                                   user.getRole().name().equals("MANAGER");

        if (!isUploader && !isAdminOrManager) {
            throw new TaskAccessDeniedException(
                    "You do not have permission to delete this attachment");
        }

        attachmentRepository.delete(attachment);
    }

    // ===============================
    // PRIVATE HELPERS
    // ===============================
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    private AttachmentListDTO mapToDTO(TaskAttachment a) {
        return new AttachmentListDTO(
                a.getId(),
                a.getTask().getId(),
                a.getUploader().getId(),
                a.getUploader().getFullName(),
                a.getOriginalName(),
                a.getMimeType(),
                a.getFileSizeBytes(),
                a.getUploadedAt()
        );
    }
}