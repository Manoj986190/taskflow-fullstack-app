package com.taskflow.taskflow_backend.service;

import com.taskflow.taskflow_backend.dto.SubtaskRequest;
import com.taskflow.taskflow_backend.dto.SubtaskResponse;
import com.taskflow.taskflow_backend.dto.SubtaskSummaryDTO;
import com.taskflow.taskflow_backend.entity.Subtask;
import com.taskflow.taskflow_backend.entity.Task;
import com.taskflow.taskflow_backend.entity.User;
import com.taskflow.taskflow_backend.exception.ResourceNotFoundException;
import com.taskflow.taskflow_backend.exception.TaskAccessDeniedException;
import com.taskflow.taskflow_backend.repository.SubtaskRepository;
import com.taskflow.taskflow_backend.repository.TaskRepository;
import com.taskflow.taskflow_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubtaskService {

    private final SubtaskRepository subtaskRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // ===============================
    // LIST
    // ===============================
    public List<SubtaskResponse> getSubtasks(Long taskId) {
        taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        return subtaskRepository.findByTask_IdOrderByCreatedAtAsc(taskId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ===============================
    // SUMMARY — { total, completed }
    // ===============================
    public SubtaskSummaryDTO getSummary(Long taskId) {
        int total = subtaskRepository.countByTask_Id(taskId);
        int completed = subtaskRepository.countCompletedByTaskId(taskId);
        return new SubtaskSummaryDTO(total, completed);
    }

    // ===============================
    // CREATE (TC-S01)
    // ===============================
    public SubtaskResponse create(Long taskId,
                                  SubtaskRequest request,
                                  String email) {
        User creator = getUserByEmail(email);
        denyViewer(creator);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User assignee = null;
        if (request.assignedToId() != null) {
            assignee = userRepository.findById(request.assignedToId())
                    .orElse(null);
        }

        Subtask subtask = Subtask.builder()
                .task(task)
                .title(request.title().trim())
                .isComplete(false)
                .assignedTo(assignee)
                .createdBy(creator)
                .build();

        return mapToResponse(subtaskRepository.save(subtask));
    }

    // ===============================
    // TOGGLE (TC-S02, TC-S03)
    // ===============================
    @Transactional
    public SubtaskResponse toggle(Long subtaskId, String email) {
        User user = getUserByEmail(email);
        denyViewer(user);

        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found"));

        boolean nowComplete = !subtask.getIsComplete();
        subtask.setIsComplete(nowComplete);
        subtask.setCompletedAt(nowComplete ? LocalDateTime.now() : null);

        return mapToResponse(subtaskRepository.save(subtask));
    }

    // ===============================
    // UPDATE
    // ===============================
    @Transactional
    public SubtaskResponse update(Long subtaskId,
                                  SubtaskRequest request,
                                  String email) {
        User user = getUserByEmail(email);
        denyViewer(user);

        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found"));

        boolean isCreator = subtask.getCreatedBy().getId().equals(user.getId());
        boolean isAdminOrManager = isAdminOrManager(user);

        if (!isCreator && !isAdminOrManager) {
            throw new TaskAccessDeniedException(
                    "Only subtask creator or Admin/Manager can edit");
        }

        subtask.setTitle(request.title().trim());

        if (request.assignedToId() != null) {
            User assignee = userRepository.findById(request.assignedToId())
                    .orElse(null);
            subtask.setAssignedTo(assignee);
        } else {
            subtask.setAssignedTo(null);
        }

        return mapToResponse(subtaskRepository.save(subtask));
    }

    // ===============================
    // DELETE (TC-S05)
    // ===============================
    @Transactional
    public void delete(Long subtaskId, String email) {
        User user = getUserByEmail(email);
        denyViewer(user);

        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found"));

        boolean isCreator = subtask.getCreatedBy().getId().equals(user.getId());
        boolean isAdminOrManager = isAdminOrManager(user);

        if (!isCreator && !isAdminOrManager) {
            throw new TaskAccessDeniedException(
                    "Only subtask creator or Admin/Manager can delete");
        }

        subtaskRepository.delete(subtask);
    }

    // ===============================
    // PRIVATE HELPERS
    // ===============================
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void denyViewer(User user) {
        if (user.getRole().name().equals("VIEWER")) {
            throw new TaskAccessDeniedException(
                    "Viewers cannot modify subtasks");
        }
    }

    private boolean isAdminOrManager(User user) {
        String role = user.getRole().name();
        return role.equals("ADMIN") || role.equals("MANAGER");
    }

    private SubtaskResponse mapToResponse(Subtask s) {
        return new SubtaskResponse(
                s.getId(),
                s.getTask().getId(),
                s.getTitle(),
                s.getIsComplete(),
                s.getAssignedTo() != null ? s.getAssignedTo().getId() : null,
                s.getAssignedTo() != null ? s.getAssignedTo().getFullName() : null,
                s.getCreatedBy().getId(),
                s.getCreatedBy().getFullName(),
                s.getCreatedAt(),
                s.getCompletedAt()
        );
    }
}