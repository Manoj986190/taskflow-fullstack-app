package com.taskflow.taskflow_backend.service;

import com.taskflow.taskflow_backend.dto.TaskRequest;
import com.taskflow.taskflow_backend.dto.TaskResponse;
import com.taskflow.taskflow_backend.dto.TaskSummaryResponse;
import com.taskflow.taskflow_backend.entity.ActionCode;
import com.taskflow.taskflow_backend.entity.Task;
import com.taskflow.taskflow_backend.entity.TaskPriority;
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
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;

    // =========================================================
    // GET ALL TASKS (Owner OR Assigned)
    // =========================================================
//     public List<TaskResponse> getTasksForUser(String email,TaskPriority priority) {

//         User user = getUserByEmail(email);
//         List<Task> tasks;
//         if (priority != null) {
//         tasks = taskRepository
//                 .findTasksByUserOrAssignedAndPriority(user, priority);
//         } else {
//         tasks = taskRepository
//                 .findByUserOrAssignedTo(user, user);
//         }

//         return tasks.stream()
//             .map(this::mapToResponse)
//             .toList();
//     }

    // =========================================================
    // GET ALL TASKS (VISIBLE TO ALL USERS)
    // =========================================================
    public List<TaskResponse> getTasksForUser(String email, TaskPriority priority) {

        List<Task> tasks;

        if (priority != null) {
                tasks = taskRepository.findByPriority(priority);
        } else {
                tasks = taskRepository.findAll();
        }

        return tasks.stream()
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
                .priority(request.getPriority() != null
                        ? request.getPriority()
                        : TaskPriority.MEDIUM)
                .user(user) // OWNER
                .assignedTo(assignedUser)
                .build();

        Task saved = taskRepository.save(task);

        activityService.log(
                        user,
                        saved,
                        ActionCode.TASK_CREATED,
                        user.getFullName() + " created task '" + saved.getTitle() + "'");

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
            activityService.log(
                            user,
                            task,
                            ActionCode.TASK_STATUS_CHANGED,
                            user.getFullName() + " changed status of '" + task.getTitle() +
                                            "' to " + task.getStatus());
            if (request.getPriority() != null) {
                    task.setPriority(request.getPriority());
                    activityService.log(
                                    user,
                                    task,
                                    ActionCode.TASK_PRIORITY_CHANGED,
                                    user.getFullName() + " changed priority of '" + task.getTitle() +
                                                    "' to " + task.getPriority());
                }

            if (request.getAssignedToUserId() != null) {
                User assignedUser = userRepository
                        .findById(request.getAssignedToUserId())
                        .orElseThrow(() ->
                                new UserNotFoundException("Assigned user not found"));

                task.setAssignedTo(assignedUser);
                activityService.log(
                                user,
                                task,
                                ActionCode.TASK_ASSIGNED,
                                user.getFullName() + " assigned '" + task.getTitle() +
                                                "' to " + assignedUser.getFullName());
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
        activityService.log(
                        getUserByEmail(email),
                        task,
                        ActionCode.TASK_DELETED,
                        getUserByEmail(email).getFullName() +
                                        " deleted task '" + task.getTitle() + "'");

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
//     private Task getTaskForView(String email,
//                                 Long taskId) {

//         User user = getUserByEmail(email);

//         Task task = taskRepository.findById(taskId)
//                 .orElseThrow(() ->
//                         new TaskNotFoundException("Task not found"));

//         boolean isOwner =
//                 task.getUser().getId().equals(user.getId());

//         boolean isAssignee =
//                 task.getAssignedTo() != null &&
//                 task.getAssignedTo().getId().equals(user.getId());

//         if (!isOwner && !isAssignee) {
//             throw new TaskAccessDeniedException(
//                     "You do not have permission to access this task");
//         }

//         return task;
//     }

    // View allowed for any logged-in user
    private Task getTaskForView(String email,
                Long taskId) {

        return taskRepository.findById(taskId)
                        .orElseThrow(() -> new TaskNotFoundException("Task not found"));
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
                task.getPriority(),   // 👈 ADD THIS
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

    public TaskSummaryResponse getTaskSummary(String email) {

            User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException("User not found"));

            int totalTasks = taskRepository.countTotalTasks(user);

            int todo = taskRepository.countTodo(user);
            int inProgress = taskRepository.countInProgress(user);
            int done = taskRepository.countDone(user);

            int high = taskRepository.countHigh(user);
            int medium = taskRepository.countMedium(user);
            int low = taskRepository.countLow(user);

            int overdue = taskRepository.countOverdue(user);

            int tasksThisWeek = taskRepository.countTasksThisWeek(user);

            double completionRate = 0;

            if (totalTasks > 0) {
                    completionRate = ((double) done / totalTasks) * 100;
                    completionRate = Math.round(completionRate * 10.0) / 10.0;
            }

            Map<String, Integer> byStatus = Map.of(
                            "todo", todo,
                            "inProgress", inProgress,
                            "done", done);

            Map<String, Integer> byPriority = Map.of(
                            "high", high,
                            "medium", medium,
                            "low", low);

            return new TaskSummaryResponse(
                            totalTasks,
                            byStatus,
                            byPriority,
                            completionRate,
                            overdue,
                            tasksThisWeek);
    }
}