package com.taskflow.taskflow_backend.controller;

import com.taskflow.taskflow_backend.dto.TaskResponse;
import com.taskflow.taskflow_backend.entity.TaskStatus;
import com.taskflow.taskflow_backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    // CREATE task
    // -------------------------------
    @PostMapping
    public TaskResponse createTask(Authentication authentication,
                                   @RequestParam String title,
                                   @RequestParam(required = false) String description,
                                   @RequestParam LocalDate dueDate,
                                   @RequestParam(defaultValue = "TODO") TaskStatus status) {

        String email = authentication.getName();
        return taskService.createTask(email, title, description, dueDate, status);
    }

    // -------------------------------
    // UPDATE task
    // -------------------------------
    @PutMapping("/{id}")
    public TaskResponse updateTask(Authentication authentication,
                                   @PathVariable Long id,
                                   @RequestParam String title,
                                   @RequestParam(required = false) String description,
                                   @RequestParam LocalDate dueDate,
                                   @RequestParam TaskStatus status) {

        String email = authentication.getName();
        return taskService.updateTask(email, id, title, description, dueDate, status);
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