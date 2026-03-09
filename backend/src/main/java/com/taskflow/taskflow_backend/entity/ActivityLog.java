package com.taskflow.taskflow_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_code", nullable = false)
    private ActionCode actionCode;

    @Column(nullable = false)
    private String message;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void created() {
        createdAt = LocalDateTime.now();
    }
}