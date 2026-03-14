package com.taskflow.taskflow_backend.controller;

import com.taskflow.taskflow_backend.dto.ManualLogRequest;
import com.taskflow.taskflow_backend.dto.TimeLogDTO;
import com.taskflow.taskflow_backend.dto.TimerStatusDTO;
import com.taskflow.taskflow_backend.service.TimeTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TimeTrackingController {

    private final TimeTrackingService timeTrackingService;

    // TIMER STATUS
    @GetMapping("/api/tasks/{taskId}/timer/status")
    public TimerStatusDTO status(@PathVariable Long taskId,
                                 Authentication auth) {
        return timeTrackingService
                .getTimerStatus(taskId, auth.getName());
    }

    // START TIMER
    @PostMapping("/api/tasks/{taskId}/timer/start")
    @ResponseStatus(HttpStatus.CREATED)
    public TimerStatusDTO start(@PathVariable Long taskId,
                                Authentication auth) {
        return timeTrackingService
                .startTimer(taskId, auth.getName());
    }

    // STOP TIMER
    @PostMapping("/api/tasks/{taskId}/timer/stop")
    public TimeLogDTO stop(@PathVariable Long taskId,
                           Authentication auth) {
        return timeTrackingService
                .stopTimer(taskId, auth.getName());
    }

    // MANUAL LOG
    @PostMapping("/api/tasks/{taskId}/time-logs")
    @ResponseStatus(HttpStatus.CREATED)
    public TimeLogDTO manual(@PathVariable Long taskId,
                             @RequestBody ManualLogRequest request,
                             Authentication auth) {
        return timeTrackingService
                .logManual(taskId, request, auth.getName());
    }

    // GET ALL LOGS
    @GetMapping("/api/tasks/{taskId}/time-logs")
    public List<TimeLogDTO> logs(@PathVariable Long taskId) {
        return timeTrackingService.getLogs(taskId);
    }

    // TOTAL
    @GetMapping("/api/tasks/{taskId}/time-logs/total")
    public Map<String, Integer> total(@PathVariable Long taskId) {
        return timeTrackingService.getTotal(taskId);
    }

    // DELETE MANUAL LOG ONLY
    @DeleteMapping("/api/time-logs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       Authentication auth) {
        timeTrackingService.deleteLog(id, auth.getName());
    }
}