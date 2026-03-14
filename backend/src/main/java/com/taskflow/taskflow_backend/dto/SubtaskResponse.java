package com.taskflow.taskflow_backend.dto;

import java.time.LocalDateTime;

public record SubtaskResponse(
        Long id,
        Long taskId,
        String title,
        Boolean isComplete,
        Long assignedToId,
        String assignedToName,
        Long createdById,
        String createdByName,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {}