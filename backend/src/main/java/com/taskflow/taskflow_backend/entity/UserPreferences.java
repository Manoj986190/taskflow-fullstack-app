package com.taskflow.taskflow_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferences {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @JsonIgnore 
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ThemePreference theme = ThemePreference.LIGHT;

    @Builder.Default
    @Column(name = "notify_assigned", nullable = false)
    private Boolean notifyAssigned = true;

    @Builder.Default
    @Column(name = "notify_comment", nullable = false)
    private Boolean notifyComment = true;

    @Builder.Default
    @Column(name = "notify_subtask", nullable = false)
    private Boolean notifySubtask = true;

    @Builder.Default
    @Column(name = "notify_overdue", nullable = false)
    private Boolean notifyOverdue = true;

    @Builder.Default
    @Column(name = "notify_team", nullable = false)
    private Boolean notifyTeam = true;

    @Builder.Default
    @Column(name = "avatar_colour", length = 7)
    private String avatarColour = "#2563EB";

    @Column(length = 200)
    private String bio;

    public enum ThemePreference { LIGHT, DARK, SYSTEM }
}