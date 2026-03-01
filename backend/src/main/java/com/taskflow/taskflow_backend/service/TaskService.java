package com.taskflow.taskflow_backend.service;

import com.taskflow.taskflow_backend.dto.TaskRequest;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // =========================================================
    // GET ALL TASKS (Owner OR Assigned)
    // =========================================================
    public List<TaskResponse> getTasksForUser(String email) {

        User user = getUserByEmail(email);

        return taskRepository
                .findByUserOrAssignedTo(user, user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // CREATE TASK (Owner Only)
    // =========================================================
    public TaskResponse createTask(String email,
                                   TaskRequest request) {

        User user = getUserByEmail(email);

        User assignedUser = null;

        if (request.getAssignedToUserId() != null) {
            assignedUser = userRepository.findById(request.getAssignedToUserId())
                    .orElseThrow(() ->
                            new UserNotFoundException("Assigned user not found"));
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .status(request.getStatus() != null
                        ? request.getStatus()
                        : TaskStatus.TODO)
                .user(user) // OWNER
                .assignedTo(assignedUser)
                .build();

        Task saved = taskRepository.save(task);

        return mapToResponse(saved);
    }

    // =========================================================
    // GET TASK BY ID (Owner OR Assignee can view)
    // =========================================================
    public TaskResponse getTaskById(String email,
                                    Long taskId) {

        Task task = getTaskForView(email, taskId);
        return mapToResponse(task);
    }

    // =========================================================
    // UPDATE TASK
    // Owner → Full Edit
    // Assignee → Status Only
    // =========================================================
    public TaskResponse updateTask(String email,
                                   Long taskId,
                                   TaskRequest request) {

        User user = getUserByEmail(email);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new TaskNotFoundException("Task not found"));

        boolean isOwner =
                task.getUser().getId().equals(user.getId());

        boolean isAssignee =
                task.getAssignedTo() != null &&
                task.getAssignedTo().getId().equals(user.getId());

        if (!isOwner && !isAssignee) {
            throw new TaskAccessDeniedException(
                    "You do not have permission to modify this task");
        }

        // ============================
        // OWNER → FULL EDIT
        // ============================
        if (isOwner) {

            task.setTitle(request.getTitle());
            task.setDescription(request.getDescription());
            task.setDueDate(request.getDueDate());
            task.setStatus(request.getStatus());

            if (request.getAssignedToUserId() != null) {
                User assignedUser = userRepository
                        .findById(request.getAssignedToUserId())
                        .orElseThrow(() ->
                                new UserNotFoundException("Assigned user not found"));

                task.setAssignedTo(assignedUser);
            } else {
                task.setAssignedTo(null);
            }
        }

        // ============================
        // ASSIGNEE → STATUS ONLY
        // ============================
        else {
            task.setStatus(request.getStatus());
        }

        Task updated = taskRepository.save(task);

        return mapToResponse(updated);
    }

    // =========================================================
    // DELETE TASK (Owner Only)
    // =========================================================
    public void deleteTask(String email,
                           Long taskId) {

        Task task = getTaskForOwnerOnly(email, taskId);
        taskRepository.delete(task);
    }

    // =========================================================
    // PRIVATE HELPERS
    // =========================================================

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));
    }

    // View allowed for Owner OR Assignee
    private Task getTaskForView(String email,
                                Long taskId) {

        User user = getUserByEmail(email);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new TaskNotFoundException("Task not found"));

        boolean isOwner =
                task.getUser().getId().equals(user.getId());

        boolean isAssignee =
                task.getAssignedTo() != null &&
                task.getAssignedTo().getId().equals(user.getId());

        if (!isOwner && !isAssignee) {
            throw new TaskAccessDeniedException(
                    "You do not have permission to access this task");
        }

        return task;
    }

    // Only Owner allowed
    private Task getTaskForOwnerOnly(String email,
                                     Long taskId) {

        User user = getUserByEmail(email);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new TaskNotFoundException("Task not found"));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new TaskAccessDeniedException(
                    "Only task owner can delete this task");
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
                task.getUpdatedAt(),
                task.getUser().getId(), // 🔥 OWNER ID ADDED
                task.getAssignedTo() != null
                        ? task.getAssignedTo().getId()
                        : null,
                task.getAssignedTo() != null
                        ? task.getAssignedTo().getFullName()
                        : null
        );
    }
}