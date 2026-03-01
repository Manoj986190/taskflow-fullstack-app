package com.taskflow.taskflow_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCommentRequestDTO {

    @NotBlank(message = "Comment body cannot be empty")
    private String body;
}