package com.taskflow.taskflow_backend.exception;

public class TaskAccessDeniedException extends RuntimeException {

    public TaskAccessDeniedException(String message) {
        super(message);
    }
}
