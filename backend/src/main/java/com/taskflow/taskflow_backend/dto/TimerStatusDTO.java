package com.taskflow.taskflow_backend.dto;

import java.time.LocalDateTime;

public record TimerStatusDTO(
        boolean running,
        LocalDateTime startTime   // null if not running
) {}