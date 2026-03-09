package com.taskflow.taskflow_backend.service;

import com.taskflow.taskflow_backend.dto.*;
import com.taskflow.taskflow_backend.entity.*;
import com.taskflow.taskflow_backend.exception.ForbiddenException;
import com.taskflow.taskflow_backend.exception.ResourceNotFoundException;
import com.taskflow.taskflow_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;

    // =========================
    // GET COMMENTS
    // =========================
    public List<CommentResponseDTO> getCommentsByTask(Long taskId) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        List<TaskComment> comments =
                commentRepository
                        .findByTaskIdOrderByCreatedAtAsc(taskId);

        return comments.stream()
                .map(comment -> mapToDTO(comment, email))
                .collect(Collectors.toList());
    }

    // =========================
    // ADD COMMENT
    // =========================
    public CommentResponseDTO addComment(
            Long taskId,
            CreateCommentRequestDTO request
    ) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User author = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found"));

        TaskComment comment = TaskComment.builder()
                .task(task)
                .author(author)
                .body(request.getBody())
                .build();

        TaskComment saved = commentRepository.save(comment);
        activityService.log(
                        author,
                        task,
                        ActionCode.COMMENT_ADDED,
                        author.getFullName() + " commented on '" + task.getTitle() + "'");

        return mapToDTO(saved, email);
    }

    // =========================
    // DELETE COMMENT
    // =========================
    public void deleteComment(Long commentId) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        TaskComment comment =
                commentRepository.findById(commentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Comment not found"));

        if (!comment.getAuthor().getEmail().equals(email)) {
            throw new ForbiddenException("You are not allowed to delete this comment");
        }

        commentRepository.delete(comment);
    }

    // =========================
    // MAPPER
    // =========================
    private CommentResponseDTO mapToDTO(
            TaskComment comment,
            String currentEmail
    ) {

        return CommentResponseDTO.builder()
                .id(comment.getId())
                .authorFullName(
                        comment.getAuthor().getFullName()
                )
                .body(comment.getBody())
                .createdAt(comment.getCreatedAt())
                .isOwner(
                        comment.getAuthor()
                                .getEmail()
                                .equals(currentEmail)
                )
                .build();
    }
}