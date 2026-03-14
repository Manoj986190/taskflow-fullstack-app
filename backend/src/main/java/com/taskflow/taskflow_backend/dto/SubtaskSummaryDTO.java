package com.taskflow.taskflow_backend.dto;

public record SubtaskSummaryDTO(
        int total,
        int completed
) {}