package com.taskflow.taskflow_backend.dto;

import java.time.LocalDateTime;

public record AttachmentListDTO(
        Long id,
        Long taskId,
        Long uploaderId,
        String uploaderName,
        String originalName,
        String mimeType,
        Long fileSizeBytes,
        LocalDateTime uploadedAt
) {}