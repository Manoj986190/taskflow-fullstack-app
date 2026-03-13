package com.taskflow.taskflow_backend.controller;

import com.taskflow.taskflow_backend.dto.AttachmentListDTO;
import com.taskflow.taskflow_backend.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    // ===============================
    // UPLOAD
    // ===============================
    @PostMapping("/api/tasks/{taskId}/attachments")
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentListDTO upload(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {

        return attachmentService.upload(taskId, file,
                authentication.getName());
    }

    // ===============================
    // LIST — metadata only
    // ===============================
    @GetMapping("/api/tasks/{taskId}/attachments")
    public List<AttachmentListDTO> list(
            @PathVariable Long taskId) {

        return attachmentService.listAttachments(taskId);
    }

    // ===============================
    // DOWNLOAD
    // ===============================
    @GetMapping("/api/attachments/{attachmentId}/download")
    public ResponseEntity<byte[]> download(
            @PathVariable Long attachmentId) {

        return attachmentService.download(attachmentId);
    }

    // ===============================
    // DELETE
    // ===============================
    @DeleteMapping("/api/attachments/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long attachmentId,
            Authentication authentication) {

        attachmentService.delete(attachmentId,
                authentication.getName());
    }
}