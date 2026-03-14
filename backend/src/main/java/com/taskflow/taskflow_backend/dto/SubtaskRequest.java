package com.taskflow.taskflow_backend.dto;

public record SubtaskRequest(
        String title,
        Long assignedToId   // nullable
) {}