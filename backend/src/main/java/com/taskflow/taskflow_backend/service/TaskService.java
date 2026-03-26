package com.taskflow.taskflow_backend.service;

import com.taskflow.taskflow_backend.dto.TaskRequest;
import com.taskflow.taskflow_backend.dto.TaskResponse;
import com.taskflow.taskflow_backend.dto.TaskSummaryResponse;
import com.taskflow.taskflow_backend.entity.ActionCode;
import com.taskflow.taskflow_backend.entity.Task;
import com.taskflow.taskflow_backend.entity.TaskPriority;
import com.taskflow.taskflow_backend.entity.TaskStatus;
import com.taskflow.taskflow_backend.entity.Team;
import com.taskflow.taskflow_backend.entity.User;
import com.taskflow.taskflow_backend.exception.ResourceNotFoundException;
import com.taskflow.taskflow_backend.exception.TaskAccessDeniedException;
import com.taskflow.taskflow_backend.exception.TaskNotFoundException;
import com.taskflow.taskflow_backend.exception.UserNotFoundException;
import com.taskflow.taskflow_backend.repository.ActivityLogRepository;
import com.taskflow.taskflow_backend.repository.TaskRepository;
import com.taskflow.taskflow_backend.repository.TeamMemberRepository;
import com.taskflow.taskflow_backend.repository.TeamRepository;
import com.taskflow.taskflow_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;
    private final TeamRepository teamRepository;  
    private final ActivityLogRepository activityLogRepository;  
    private final TeamMemberRepository teamMemberRepository;

    // =========================================================
    // GET TASKS (SECURE + CLEAN)
    // =========================================================
    public List<TaskResponse> getTasksForUser(String email, TaskPriority priority) {

            User user = getUserByEmail(email);

            List<Task> tasks = getTasksByRole(user, priority);

            return tasks.stream()
                            .map(this::mapToResponse)
                            .toList();
    }

    // =========================================================
    // CORE ROLE + TEAM FILTER LOGIC
    // =========================================================
    private List<Task> getTasksByRole(User user, TaskPriority priority) {

            // 🔹 ADMIN → all tasks
            if (isAdmin(user)) {
                    return (priority != null)
                                    ? taskRepository.findByPriority(priority)
                                    : taskRepository.findAll();
            }

            // 🔹 MANAGER → tasks of managed teams
            if (isManager(user)) {
                    List<Long> teamIds = teamRepository
                                    .findByManager_Id(user.getId())
                                    .stream()
                                    .map(Team::getId)
                                    .toList();

                    if (teamIds.isEmpty())
                            return List.of();

                    return (priority != null)
                                    ? taskRepository.findByTeam_IdInAndPriority(teamIds, priority)
                                    : taskRepository.findByTeam_IdIn(teamIds);
            }

            // 🔹 MEMBER + VIEWER → tasks of joined teams
            List<Long> teamIds = teamMemberRepository
                            .findByUser_Id(user.getId())
                            .stream()
                            .map(tm -> tm.getTeam().getId())
                            .toList();

            if (teamIds.isEmpty())
                    return List.of();

            return (priority != null)
                            ? taskRepository.findByTeam_IdInAndPriority(teamIds, priority)
                            : taskRepository.findByTeam_IdIn(teamIds);
    }

    // =========================================================
    // ROLE HELPERS
    // =========================================================
    private boolean isAdmin(User user) {
            return user.getRole().name().equals("ADMIN");
    }

    private boolean isManager(User user) {
            return user.getRole().name().equals("MANAGER");
    }

    // =========================================================
    // CREATE TASK
    // =========================================================
    public TaskResponse createTask(String email, TaskRequest request) {

        User user = getUserByEmail(email);

        if (user.getRole().name().equals("VIEWER")) {
            throw new TaskAccessDeniedException(
                    "Viewers are not allowed to create tasks");
        }

        User assignedUser = null;
        if (request.getAssignedToUserId() != null) {
            assignedUser = userRepository.findById(request.getAssignedToUserId())
                    .orElseThrow(() -> new UserNotFoundException("Assigned user not found"));
        }

        // ✅ ADD — prevent assigning task to VIEWER
        if (assignedUser != null &&
                        assignedUser.getRole().name().equals("VIEWER")) {
                throw new TaskAccessDeniedException(
                                "Tasks cannot be assigned to Viewers");
        }

        // ✅ ADD — resolve team if provided
        Team team = null;
        if (request.getTeamId() != null) {
            team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team not found"));
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .user(user)
                .assignedTo(assignedUser)
                .team(team)  // ✅ ADD
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
    // GET TASK BY ID
    // =========================================================
    public TaskResponse getTaskById(String email, Long taskId) {
        Task task = getTaskForView(email, taskId);
        return mapToResponse(task);
    }

    // =========================================================
    // UPDATE TASK
    // =========================================================
    public TaskResponse updateTask(String email, Long taskId, TaskRequest request) {

        User user = getUserByEmail(email);

        if (user.getRole().name().equals("VIEWER")) {
            throw new TaskAccessDeniedException(
                    "Viewers are not allowed to modify tasks");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        boolean isOwner = task.getUser().getId().equals(user.getId());
        boolean isAssignee = task.getAssignedTo() != null &&
                task.getAssignedTo().getId().equals(user.getId());

        if (!isOwner && !isAssignee) {
            throw new TaskAccessDeniedException(
                    "You do not have permission to modify this task");
        }

        if (isOwner) {
            task.setTitle(request.getTitle());
            task.setDescription(request.getDescription());
            task.setDueDate(request.getDueDate());
            task.setStatus(request.getStatus());

            activityService.log(user, task, ActionCode.TASK_STATUS_CHANGED,
                    user.getFullName() + " changed status of '" + task.getTitle() +
                    "' to " + task.getStatus());

            if (request.getPriority() != null) {
                task.setPriority(request.getPriority());
                activityService.log(user, task, ActionCode.TASK_PRIORITY_CHANGED,
                        user.getFullName() + " changed priority of '" + task.getTitle() +
                        "' to " + task.getPriority());
            }

            if (request.getAssignedToUserId() != null) {
                User assignedUser = userRepository.findById(request.getAssignedToUserId())
                        .orElseThrow(() -> new UserNotFoundException("Assigned user not found"));
                task.setAssignedTo(assignedUser);
                activityService.log(user, task, ActionCode.TASK_ASSIGNED,
                        user.getFullName() + " assigned '" + task.getTitle() +
                        "' to " + assignedUser.getFullName());
            } else {
                task.setAssignedTo(null);
            }
        } else {
            task.setStatus(request.getStatus());
        }

        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    // =========================================================
    // DELETE TASK
    // =========================================================
    @Transactional 
    public void deleteTask(String email, Long taskId) {

        User user = getUserByEmail(email);

        if (user.getRole().name().equals("VIEWER")) {
            throw new TaskAccessDeniedException(
                    "Viewers are not allowed to delete tasks");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        // ✅ ADMIN can delete any task, MANAGER can delete any task
        // Only restrict for MEMBER
        boolean isOwner = task.getUser().getId().equals(user.getId());
        boolean isAdminOrManager = user.getRole().name().equals("ADMIN") ||
                user.getRole().name().equals("MANAGER");

        if (!isOwner && !isAdminOrManager) { // ✅ FIX
            throw new TaskAccessDeniedException(
                    "Only task owner can delete this task");
        }

        activityService.log(
                user,
                task,
                ActionCode.TASK_DELETED,
                user.getFullName() + " deleted task '" + task.getTitle() + "'");

        activityLogRepository.deleteByTask(task);
        taskRepository.delete(task);
    }

    // =========================================================
    // PRIVATE HELPERS
    // =========================================================

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private Task getTaskForView(String email, Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
    }

    // ✅ UPDATED — added teamId + teamName
    private TaskResponse mapToResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getStatus(),
                task.getPriority(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getUser().getId(),
                task.getAssignedTo() != null ? task.getAssignedTo().getId() : null,
                task.getAssignedTo() != null ? task.getAssignedTo().getFullName() : null,
                task.getTeam() != null ? task.getTeam().getId() : null,    // ✅ ADD
                task.getTeam() != null ? task.getTeam().getName() : null   // ✅ ADD
        );
    }

    // =========================================================
    // ANALYTICS
    // =========================================================
    public TaskSummaryResponse getTaskSummary(String email) {

            User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException("User not found"));

            boolean isPrivileged = isPrivileged(user);

            int totalTasks = isPrivileged
                            ? taskRepository.countAllTasks()
                            : taskRepository.countTotalTasks(user);

            int todo = isPrivileged
                            ? taskRepository.countAllTodo()
                            : taskRepository.countTodo(user);

            int inProgress = isPrivileged
                            ? taskRepository.countAllInProgress()
                            : taskRepository.countInProgress(user);

            int done = isPrivileged
                            ? taskRepository.countAllDone()
                            : taskRepository.countDone(user);

            int high = isPrivileged
                            ? taskRepository.countAllHigh()
                            : taskRepository.countHigh(user);

            int medium = isPrivileged
                            ? taskRepository.countAllMedium()
                            : taskRepository.countMedium(user);

            int low = isPrivileged
                            ? taskRepository.countAllLow()
                            : taskRepository.countLow(user);

            int overdue = isPrivileged
                            ? taskRepository.countAllOverdue()
                            : taskRepository.countOverdue(user);

            int tasksThisWeek = isPrivileged
                            ? taskRepository.countAllTasksThisWeek()
                            : taskRepository.countTasksThisWeek(user);

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

    //Helper method for isPrivileged
    private boolean isPrivileged(User user) {
            return user.getRole().name().equals("ADMIN") ||
                            user.getRole().name().equals("MANAGER") ||
                            user.getRole().name().equals("VIEWER");
    }
}