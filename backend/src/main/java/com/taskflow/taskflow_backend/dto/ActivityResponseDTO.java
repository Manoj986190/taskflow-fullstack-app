package com.taskflow.taskflow_backend.dto;

import com.taskflow.taskflow_backend.entity.ActionCode;

import java.time.LocalDateTime;

public record ActivityResponseDTO(

        Long id,
        ActionCode actionCode,
        String message,
        LocalDateTime createdAt

) {}