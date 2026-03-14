package com.taskflow.taskflow_backend.dto;

import java.time.LocalDateTime;

public record SessionDTO(
        String jti,
        String deviceHint,
        LocalDateTime revokedAt,
        boolean current
) {}