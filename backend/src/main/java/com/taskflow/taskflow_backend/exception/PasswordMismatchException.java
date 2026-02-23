package com.taskflow.taskflow_backend.exception;

public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException(String message) {
        super(message);
    }
}