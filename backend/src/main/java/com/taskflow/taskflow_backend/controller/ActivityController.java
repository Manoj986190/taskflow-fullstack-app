package com.taskflow.taskflow_backend.controller;

import com.taskflow.taskflow_backend.dto.ActivityResponseDTO;
import com.taskflow.taskflow_backend.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityLogRepository repo;

    @GetMapping
    public List<ActivityResponseDTO> getActivityFeed() {

        return repo.findTop20ByOrderByCreatedAtDesc()
                .stream()
                .map(log -> new ActivityResponseDTO(
                        log.getId(),
                        log.getActionCode(),
                        log.getMessage(),
                        log.getCreatedAt()))
                .toList();
    }
}