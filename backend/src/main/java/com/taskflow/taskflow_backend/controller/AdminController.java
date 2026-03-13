package com.taskflow.taskflow_backend.controller;

import com.taskflow.taskflow_backend.entity.Role;
import com.taskflow.taskflow_backend.entity.User;
import com.taskflow.taskflow_backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // ===============================
    // GET ALL USERS
    // ===============================
    @GetMapping("/users")
    public List<User> getUsers() {
        return adminService.getAllUsers();
    }

    // ===============================
    // CHANGE ROLE
    // ===============================
    @PatchMapping("/users/{id}/role")
    public User changeRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {  // ✅ accept object

        Role role = Role.valueOf(body.get("role"));   // ✅ extract role
        return adminService.updateUserRole(id, role);
    }

    // ===============================
    // CHANGE STATUS
    // ===============================
    @PatchMapping("/users/{id}/status")
    public User changeStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {  // ✅ accept object

        Boolean active = body.get("isActive");         // ✅ extract isActive
        return adminService.updateUserStatus(id, active);
    }
}