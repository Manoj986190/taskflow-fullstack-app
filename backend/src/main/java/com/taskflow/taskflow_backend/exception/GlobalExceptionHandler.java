package com.taskflow.taskflow_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<?> handleEmailExists(EmailAlreadyExistsException ex) {

        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<?> handleInvalidCredentials(InvalidCredentialsException ex) {

        return buildResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<?> handlePasswordMismatch(PasswordMismatchException ex) {

        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        return buildResponse(message, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(String message, HttpStatus status) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("message", message);

        return new ResponseEntity<>(body, status);
    }
}