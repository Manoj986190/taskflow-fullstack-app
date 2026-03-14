package com.taskflow.taskflow_backend.service;

import com.taskflow.taskflow_backend.dto.ManualLogRequest;
import com.taskflow.taskflow_backend.dto.TimeLogDTO;
import com.taskflow.taskflow_backend.dto.TimerStatusDTO;
import com.taskflow.taskflow_backend.entity.ActiveTimer;
import com.taskflow.taskflow_backend.entity.Task;
import com.taskflow.taskflow_backend.entity.TaskTimeLog;
import com.taskflow.taskflow_backend.entity.User;
import com.taskflow.taskflow_backend.exception.ResourceNotFoundException;
import com.taskflow.taskflow_backend.exception.TaskAccessDeniedException;
import com.taskflow.taskflow_backend.repository.ActiveTimerRepository;
import com.taskflow.taskflow_backend.repository.TaskRepository;
import com.taskflow.taskflow_backend.repository.TaskTimeLogRepository;
import com.taskflow.taskflow_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TimeTrackingService {

    private final ActiveTimerRepository activeTimerRepository;
    private final TaskTimeLogRepository timeLogRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // ===============================
    // TIMER STATUS
    // ===============================
    public TimerStatusDTO getTimerStatus(Long taskId, String email) {
        User user = getUserByEmail(email);
        return activeTimerRepository
                .findByTask_IdAndUser_Id(taskId, user.getId())
                .map(t -> new TimerStatusDTO(true, t.getStartTime()))
                .orElse(new TimerStatusDTO(false, null));
    }

    // ===============================
    // START TIMER
    // ===============================
    @Transactional
    public TimerStatusDTO startTimer(Long taskId, String email) {
        User user = getUserByEmail(email);
        denyViewer(user);

        Task task = getTask(taskId);

        // 409 if already running
        if (activeTimerRepository
                .findByTask_IdAndUser_Id(taskId, user.getId())
                .isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Timer already running for this task");
        }

        ActiveTimer timer = ActiveTimer.builder()
                .task(task)
                .user(user)
                .startTime(LocalDateTime.now())
                .build();

        activeTimerRepository.save(timer);
        return new TimerStatusDTO(true, timer.getStartTime());
    }

    // ===============================
    // STOP TIMER
    // ===============================
    @Transactional
    public TimeLogDTO stopTimer(Long taskId, String email) {
        User user = getUserByEmail(email);
        denyViewer(user);

        ActiveTimer timer = activeTimerRepository
                .findByTask_IdAndUser_Id(taskId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No active timer found for this task"));

        // Compute duration in minutes
        long minutes = ChronoUnit.MINUTES.between(
                timer.getStartTime(), LocalDateTime.now());

        if (minutes < 1) minutes = 1; // minimum 1 minute

        Task task = getTask(taskId);

        TaskTimeLog log = TaskTimeLog.builder()
                .task(task)
                .loggedBy(user)
                .durationMinutes((int) minutes)
                .logDate(LocalDate.now())
                .isManual(false)
                .note("Timer session")
                .build();

        TaskTimeLog saved = timeLogRepository.save(log);
        activeTimerRepository.delete(timer);

        return mapToDTO(saved);
    }

    // ===============================
    // MANUAL LOG
    // ===============================
    @Transactional
    public TimeLogDTO logManual(Long taskId,
                                ManualLogRequest request,
                                String email) {
        User user = getUserByEmail(email);
        denyViewer(user);

        Task task = getTask(taskId);

        int totalMinutes = ((request.hours() != null
                ? request.hours() : 0) * 60)
                + (request.minutes() != null
                ? request.minutes() : 0);

        if (totalMinutes < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Duration must be at least 1 minute");
        }

        TaskTimeLog log = TaskTimeLog.builder()
                .task(task)
                .loggedBy(user)
                .durationMinutes(totalMinutes)
                .logDate(request.logDate() != null
                        ? request.logDate() : LocalDate.now())
                .note(request.note())
                .isManual(true)
                .build();

        return mapToDTO(timeLogRepository.save(log));
    }

    // ===============================
    // GET ALL LOGS
    // ===============================
    public List<TimeLogDTO> getLogs(Long taskId) {
        getTask(taskId);
        return timeLogRepository
                .findByTask_IdOrderByLogDateDesc(taskId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // ===============================
    // TOTAL MINUTES
    // ===============================
    public Map<String, Integer> getTotal(Long taskId) {
        getTask(taskId);
        int total = timeLogRepository.sumDurationByTaskId(taskId);
        return Map.of("totalMinutes", total);
    }

    // ===============================
    // DELETE MANUAL LOG ONLY
    // ===============================
    @Transactional
    public void deleteLog(Long logId, String email) {
        User user = getUserByEmail(email);

        TaskTimeLog log = timeLogRepository.findById(logId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Log not found"));

        // Timer-generated entries cannot be deleted
        if (!log.getIsManual()) {
            throw new TaskAccessDeniedException(
                    "Timer-generated entries cannot be deleted");
        }

        boolean isOwner = log.getLoggedBy().getId()
                .equals(user.getId());
        boolean isAdminOrManager =
                user.getRole().name().equals("ADMIN") ||
                user.getRole().name().equals("MANAGER");

        if (!isOwner && !isAdminOrManager) {
            throw new TaskAccessDeniedException(
                    "You cannot delete this log entry");
        }

        timeLogRepository.delete(log);
    }

    // ===============================
    // HELPERS
    // ===============================
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    private Task getTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found"));
    }

    private void denyViewer(User user) {
        if (user.getRole().name().equals("VIEWER")) {
            throw new TaskAccessDeniedException(
                    "Viewers cannot track time");
        }
    }

    private TimeLogDTO mapToDTO(TaskTimeLog log) {
        return new TimeLogDTO(
                log.getId(),
                log.getTask().getId(),
                log.getLoggedBy().getId(),
                log.getLoggedBy().getFullName(),
                log.getDurationMinutes(),
                log.getLogDate(),
                log.getNote(),
                log.getIsManual(),
                log.getCreatedAt()
        );
    }
}