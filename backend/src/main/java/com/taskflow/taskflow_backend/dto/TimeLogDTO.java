package com.taskflow.taskflow_backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TimeLogDTO(
        Long id,
        Long taskId,
        Long loggedById,
        String loggedByName,
        Integer durationMinutes,
        LocalDate logDate,
        String note,
        Boolean isManual,
        LocalDateTime createdAt
) {}