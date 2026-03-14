package com.taskflow.taskflow_backend.dto;

public record UserProfileDTO(
        Long id,
        String fullName,
        String email,
        String role,
        String avatarColour,
        String bio
) {}