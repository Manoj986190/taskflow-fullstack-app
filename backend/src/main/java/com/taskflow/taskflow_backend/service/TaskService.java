package com.taskflow.taskflow_backend.service;

import com.taskflow.taskflow_backend.dto.TaskResponse;
import com.taskflow.taskflow_backend.entity.Task;
import com.taskflow.taskflow_backend.entity.TaskStatus;
import com.taskflow.taskflow_backend.entity.User;
import com.taskflow.taskflow_backend.exception.TaskAccessDeniedException;
import com.taskflow.taskflow_backend.exception.TaskNotFoundException;
import com.taskflow.taskflow_backend.exception.UserNotFoundException;
import com.taskflow.taskflow_backend.repository.TaskRepository;
import com.taskflow.taskflow_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // -------------------------------
    // Get all tasks for logged-in user
    // -------------------------------
    public List<TaskResponse> getTasksForUser(String email) {
        User user = getUserByEmail(email);

        return taskRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // -------------------------------
    // Create new task
    // -------------------------------
    public TaskResponse createTask(String email,
                                   String title,
                                   String description,
                                   LocalDate dueDate,
                                   TaskStatus status) {

        User user = getUserByEmail(email);

        Task task = Task.builder()
                .title(title)
                .description(description)
                .dueDate(dueDate)
                .status(status)
                .user(user)
                .build();

        Task saved = taskRepository.save(task);
        return mapToResponse(saved);
    }

    // -------------------------------
    // Get task by ID
    // -------------------------------
    public TaskResponse getTaskById(String email, Long taskId) {
        Task task = getTaskForUser(email, taskId);
        return mapToResponse(task);
    }

    // -------------------------------
    // Update task
    // -------------------------------
    public TaskResponse updateTask(String email,
                                   Long taskId,
                                   String title,
                                   String description,
                                   LocalDate dueDate,
                                   TaskStatus status) {

        Task task = getTaskForUser(email, taskId);

        task.setTitle(title);
        task.setDescription(description);
        task.setDueDate(dueDate);
        task.setStatus(status);

        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    // -------------------------------
    // Delete task
    // -------------------------------
    public void deleteTask(String email, Long taskId) {
        Task task = getTaskForUser(email, taskId);
        taskRepository.delete(task);
    }

    // ===============================
    // PRIVATE HELPERS
    // ===============================

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private Task getTaskForUser(String email, Long taskId) {

        User user = getUserByEmail(email);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new TaskAccessDeniedException("You do not have permission to access this task");
        }

        return task;
    }

    private TaskResponse mapToResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getStatus(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}