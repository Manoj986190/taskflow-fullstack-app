package com.taskflow.taskflow_backend.controller;

import com.taskflow.taskflow_backend.dto.TaskRequest;
import com.taskflow.taskflow_backend.dto.TaskResponse;
import com.taskflow.taskflow_backend.dto.TaskSummaryResponse;
import com.taskflow.taskflow_backend.entity.TaskPriority;
import com.taskflow.taskflow_backend.entity.TaskStatus;
import com.taskflow.taskflow_backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // ===============================
    // GET all tasks
    // ===============================
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(
            Authentication authentication,
            @RequestParam(required = false) TaskPriority priority) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                taskService.getTasksForUser(email, priority));
    }

    @GetMapping("/summary")
    public ResponseEntity<TaskSummaryResponse> getTaskSummary(Authentication authentication) {

            String email = authentication.getName();

            return ResponseEntity.ok(
                            taskService.getTaskSummary(email));
    }

    // ===============================
    // GET single task by ID
    // ===============================
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            Authentication authentication,
            @PathVariable Long id) {

        String email = authentication.getName();
        return ResponseEntity.ok(
                taskService.getTaskById(email, id)
        );
    }

    // ===============================
    // CREATE task
    // ===============================
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public ResponseEntity<TaskResponse> createTask(
            Authentication authentication,
            @RequestBody TaskRequest request) {

        String email = authentication.getName();
        request.setStatus(TaskStatus.TODO);

        TaskResponse response = taskService.createTask(email, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // ===============================
    // UPDATE task
    // ===============================
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody TaskRequest request) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                taskService.updateTask(email, id, request)
        );
    }

    // ===============================
    // DELETE task
    // ===============================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            Authentication authentication,
            @PathVariable Long id) {

        String email = authentication.getName();

        taskService.deleteTask(email, id);

        return ResponseEntity.noContent().build();
    }
}