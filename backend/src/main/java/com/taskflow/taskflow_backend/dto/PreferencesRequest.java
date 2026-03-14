package com.taskflow.taskflow_backend.dto;

public record PreferencesRequest(
        String theme,              // LIGHT, DARK, SYSTEM — nullable
        Boolean notifyAssigned,
        Boolean notifyComment,
        Boolean notifySubtask,
        Boolean notifyOverdue,
        Boolean notifyTeam
) {}