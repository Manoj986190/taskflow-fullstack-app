package com.taskflow.taskflow_backend.controller;

import com.taskflow.taskflow_backend.dto.*;
import com.taskflow.taskflow_backend.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/tasks/{taskId}/comments")
    public ResponseEntity<List<CommentResponseDTO>>
    getComments(@PathVariable Long taskId) {

        return ResponseEntity.ok(
                commentService.getCommentsByTask(taskId)
        );
    }

    @PostMapping("/tasks/{taskId}/comments")
    public ResponseEntity<CommentResponseDTO>
    addComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateCommentRequestDTO request
    ) {

        return ResponseEntity.status(201)
                .body(
                        commentService.addComment(taskId, request)
                );
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void>
    deleteComment(@PathVariable Long commentId) {

        commentService.deleteComment(commentId);

        return ResponseEntity.noContent().build();
    }
}