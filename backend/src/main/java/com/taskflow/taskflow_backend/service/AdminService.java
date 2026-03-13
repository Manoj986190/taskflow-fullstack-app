package com.taskflow.taskflow_backend.service;

import com.taskflow.taskflow_backend.entity.Role;
import com.taskflow.taskflow_backend.entity.User;
import com.taskflow.taskflow_backend.exception.ResourceNotFoundException;
import com.taskflow.taskflow_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUserRole(Long id, Role role) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        user.setRole(role);

        return userRepository.save(user);
    }

    public User updateUserStatus(Long id, Boolean active) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        user.setIsActive(active);

        return userRepository.save(user);
    }
}