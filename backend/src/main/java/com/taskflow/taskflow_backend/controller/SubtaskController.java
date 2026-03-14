package com.taskflow.taskflow_backend.controller;

import com.taskflow.taskflow_backend.dto.SubtaskRequest;
import com.taskflow.taskflow_backend.dto.SubtaskResponse;
import com.taskflow.taskflow_backend.dto.SubtaskSummaryDTO;
import com.taskflow.taskflow_backend.service.SubtaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SubtaskController {

    private final SubtaskService subtaskService;

    // LIST
    @GetMapping("/api/tasks/{taskId}/subtasks")
    public List<SubtaskResponse> list(@PathVariable Long taskId) {
        return subtaskService.getSubtasks(taskId);
    }

    // SUMMARY — for dashboard progress bar (TC-S07)
    @GetMapping("/api/tasks/{taskId}/subtasks/summary")
    public SubtaskSummaryDTO summary(@PathVariable Long taskId) {
        return subtaskService.getSummary(taskId);
    }

    // CREATE
    @PostMapping("/api/tasks/{taskId}/subtasks")
    @ResponseStatus(HttpStatus.CREATED)
    public SubtaskResponse create(@PathVariable Long taskId,
                                  @RequestBody SubtaskRequest request,
                                  Authentication auth) {
        return subtaskService.create(taskId, request, auth.getName());
    }

    // TOGGLE
    @PatchMapping("/api/subtasks/{id}/toggle")
    public SubtaskResponse toggle(@PathVariable Long id,
                                  Authentication auth) {
        return subtaskService.toggle(id, auth.getName());
    }

    // UPDATE
    @PutMapping("/api/subtasks/{id}")
    public SubtaskResponse update(@PathVariable Long id,
                                  @RequestBody SubtaskRequest request,
                                  Authentication auth) {
        return subtaskService.update(id, request, auth.getName());
    }

    // DELETE
    @DeleteMapping("/api/subtasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       Authentication auth) {
        subtaskService.delete(id, auth.getName());
    }
}