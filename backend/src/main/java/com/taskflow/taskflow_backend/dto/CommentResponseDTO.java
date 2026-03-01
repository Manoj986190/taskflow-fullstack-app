package com.taskflow.taskflow_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CommentResponseDTO {

    private Long id;
    private String authorFullName;
    private String body;
    private LocalDateTime createdAt;

    @JsonProperty("isOwner")
    private boolean isOwner;
}