package com.taskflow.taskflow_backend.service;

import com.taskflow.taskflow_backend.dto.LoginRequest;
import com.taskflow.taskflow_backend.dto.LoginResponse;
import com.taskflow.taskflow_backend.dto.RegisterRequest;
import com.taskflow.taskflow_backend.entity.User;
import com.taskflow.taskflow_backend.exception.EmailAlreadyExistsException;
import com.taskflow.taskflow_backend.exception.InvalidCredentialsException;
import com.taskflow.taskflow_backend.exception.PasswordMismatchException;
import com.taskflow.taskflow_backend.repository.UserRepository;
import com.taskflow.taskflow_backend.security.JwtService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public void register(RegisterRequest request) {

        // 1️⃣ Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        // 2️⃣ Check if passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        // 3️⃣ Hash the password
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 4️⃣ Create User entity
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(encodedPassword)
                .build();

        // 5️⃣ Save to database
        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail(),user.getId());

        return new LoginResponse(
                token,
                "Bearer",
                1000 * 60 * 60 * 24  // 24 hours in milliseconds
        );
    }
}