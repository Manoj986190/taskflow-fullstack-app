package com.taskflow.taskflow_backend.controller;

import com.taskflow.taskflow_backend.dto.UserSummaryDTO;
import com.taskflow.taskflow_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    public List<UserSummaryDTO> getUsers(Authentication authentication) {

        // Requires valid JWT automatically via security config

        return userRepository.findAll()
                .stream()
                .map(user -> new UserSummaryDTO(
                        user.getId(),
                        user.getFullName(),
                        user.getRole().name()  // ✅ ADD THIS
                ))
                .toList();
    }
}
