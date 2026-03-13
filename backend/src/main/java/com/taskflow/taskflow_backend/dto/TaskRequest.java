package com.taskflow.taskflow_backend.dto;

import com.taskflow.taskflow_backend.entity.TaskPriority;
import com.taskflow.taskflow_backend.entity.TaskStatus;

import jakarta.validation.constraints.FutureOrPresent;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskRequest {

    private String title;
    private String description;

    // MUST match entity type
    @FutureOrPresent(message = "Due date must be today or in the future")
    private LocalDate dueDate;

    // MUST match enum type
    private TaskStatus status;

    private Long assignedToUserId;

    private TaskPriority priority;

    private Long teamId;  // ✅ ADD
}