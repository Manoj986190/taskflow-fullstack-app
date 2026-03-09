package com.taskflow.taskflow_backend.service;

import com.taskflow.taskflow_backend.entity.*;
import com.taskflow.taskflow_backend.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityLogRepository activityRepo;

    public void log(User actor, Task task, ActionCode code, String message) {

        ActivityLog log = ActivityLog.builder()
                .actor(actor)
                .task(task)
                .actionCode(code)
                .message(message)
                .build();

        activityRepo.save(log);
    }

}