package com.taskflow.taskflow_backend.controller;

import com.taskflow.taskflow_backend.dto.LoginRequest;
import com.taskflow.taskflow_backend.dto.LoginResponse;
import com.taskflow.taskflow_backend.dto.MessageResponse;
import com.taskflow.taskflow_backend.dto.RegisterRequest;
import com.taskflow.taskflow_backend.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        return ResponseEntity.status(201)
                .body(new MessageResponse("User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }
}