package com.taskflow.taskflow_backend.dto;

public record UpdateProfileRequest(
        String fullName,
        String avatarColour,
        String bio,
        String currentPassword   // required only if email changes
) {}