package com.taskflow.taskflow_backend.controller;

import com.taskflow.taskflow_backend.dto.TaskRequest;
import com.taskflow.taskflow_backend.dto.TaskResponse;
import com.taskflow.taskflow_backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // -------------------------------
    // GET all tasks
    // -------------------------------
    @GetMapping
    public List<TaskResponse> getTasks(Authentication authentication) {
        String email = authentication.getName();
        return taskService.getTasksForUser(email);
    }

    // -------------------------------
    // GET task by ID
    // -------------------------------
    @GetMapping("/{id}")
    public TaskResponse getTaskById(Authentication authentication,
                                    @PathVariable Long id) {

        String email = authentication.getName();
        return taskService.getTaskById(email, id);
    }

    // -------------------------------
    // CREATE task (JSON BODY)
    // -------------------------------
    @PostMapping
    public TaskResponse createTask(Authentication authentication,
                                   @RequestBody TaskRequest request) {

        String email = authentication.getName();
        return taskService.createTask(email, request);
    }

    // -------------------------------
    // UPDATE task (JSON BODY)
    // -------------------------------
    @PutMapping("/{id}")
    public TaskResponse updateTask(Authentication authentication,
                                   @PathVariable Long id,
                                   @RequestBody TaskRequest request) {

        String email = authentication.getName();
        return taskService.updateTask(email, id, request);
    }

    // -------------------------------
    // DELETE task
    // -------------------------------
    @DeleteMapping("/{id}")
    public void deleteTask(Authentication authentication,
                           @PathVariable Long id) {

        String email = authentication.getName();
        taskService.deleteTask(email, id);
    }
}