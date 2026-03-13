package com.taskflow.taskflow_backend.dto;

import com.taskflow.taskflow_backend.entity.TaskPriority;
import com.taskflow.taskflow_backend.entity.TaskStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        LocalDate dueDate,
        TaskStatus status,
        TaskPriority priority,   // ✅ NEW FIELD
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        Long userId,   

        // 🔥 NEW F-EXT-02 FIELDS
        Long assignedToUserId,
        String assignedToFullName,

        Long teamId,      // ✅ ADD
        String teamName  // ✅ ADD
) {}