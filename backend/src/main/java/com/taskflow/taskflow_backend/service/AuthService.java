package com.taskflow.taskflow_backend.service;

import com.taskflow.taskflow_backend.dto.LoginRequest;
import com.taskflow.taskflow_backend.dto.LoginResponse;
import com.taskflow.taskflow_backend.dto.RegisterRequest;
import com.taskflow.taskflow_backend.entity.Role;
import com.taskflow.taskflow_backend.entity.User;
import com.taskflow.taskflow_backend.exception.EmailAlreadyExistsException;
import com.taskflow.taskflow_backend.exception.InvalidCredentialsException;
import com.taskflow.taskflow_backend.exception.PasswordMismatchException;
import com.taskflow.taskflow_backend.repository.UserRepository;
import com.taskflow.taskflow_backend.security.JwtService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // SRS requirement: first user can become ADMIN
    @Value("${taskflow.security.first-user-admin:true}")
    private boolean firstUserAdmin;

    // ===============================
    // REGISTER
    // ===============================

    public void register(RegisterRequest request) {

        // 1️⃣ Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        // 2️⃣ Check password match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        // 3️⃣ Hash password
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 4️⃣ Create user entity
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(encodedPassword)
                .build();

        // 5️⃣ Assign role according to SRS
        if (firstUserAdmin && userRepository.count() == 0) {
            user.setRole(Role.ADMIN);
        } else {
            user.setRole(Role.MEMBER);
        }

        // 6️⃣ Activate user
        user.setIsActive(true);

        // 7️⃣ Save user
        userRepository.save(user);
    }

    // ===============================
    // LOGIN
    // ===============================

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password"));

        // password check
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // account status check
        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("Account is deactivated");
        }

        // generate JWT
        String token = jwtService.generateToken(
                user.getEmail(),
                user.getId(),
                user.getFullName(),
                user.getRole().name()
        );

        return new LoginResponse(
                token,
                "Bearer",
                1000 * 60 * 60 * 24    // 24 hours in milliseconds
        );
    }
}