package com.taskflow.taskflow_backend.dto;

import java.time.LocalDate;

public record ManualLogRequest(
        Integer hours,
        Integer minutes,
        LocalDate logDate,
        String note          // nullable
) {}