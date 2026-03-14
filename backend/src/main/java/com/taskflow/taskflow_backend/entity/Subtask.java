package com.taskflow.taskflow_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subtasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subtask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===============================
    // FK → tasks (CASCADE handled by Task entity)
    // ===============================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    // ===============================
    // FIELDS
    // ===============================
    @Column(nullable = false, length = 300)
    private String title;

    @Builder.Default
    @Column(name = "is_complete", nullable = false)
    private Boolean isComplete = false;

    // ===============================
    // FK → users (assignee — nullable)
    // ===============================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to",
                foreignKey = @ForeignKey(name = "fk_subtask_assigned_to"))
    private User assignedTo;

    // ===============================
    // FK → users (creator)
    // ===============================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // ===============================
    // TIMESTAMPS
    // ===============================
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}