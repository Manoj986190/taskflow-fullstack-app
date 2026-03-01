package com.taskflow.taskflow_backend.dto;

import com.taskflow.taskflow_backend.entity.TaskStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskRequest {

    private String title;
    private String description;

    // MUST match entity type
    private LocalDate dueDate;

    // MUST match enum type
    private TaskStatus status;

    private Long assignedToUserId;
}