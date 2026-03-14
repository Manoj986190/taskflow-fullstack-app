package com.taskflow.taskflow_backend.dto;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword,
        String confirmPassword
) {}