package com.taskflow.taskflow_backend.dto;

import java.util.Map;

public record TaskSummaryResponse(

        int totalTasks,

        Map<String, Integer> byStatus,

        Map<String, Integer> byPriority,

        double completionRate,

        int overdueCount,

        int tasksThisWeek

) {}
