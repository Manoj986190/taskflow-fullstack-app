package com.taskflow.taskflow_backend.dto;

public record UserSummaryDTO(
        Long id,
        String fullName,
        String role  // ✅ ADD THIS
) {}