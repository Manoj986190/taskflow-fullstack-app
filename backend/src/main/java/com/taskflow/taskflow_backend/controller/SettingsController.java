package com.taskflow.taskflow_backend.controller;

import com.taskflow.taskflow_backend.dto.*;
import com.taskflow.taskflow_backend.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    // GET PROFILE
    @GetMapping
    public UserProfileDTO getProfile(Authentication auth) {
        return settingsService.getProfile(auth.getName());
    }

    // UPDATE PROFILE
    @PatchMapping("/profile")
    public UserProfileDTO updateProfile(
            @RequestBody UpdateProfileRequest req,
            Authentication auth) {
        return settingsService.updateProfile(auth.getName(), req);
    }

    // CHANGE PASSWORD
    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @RequestBody ChangePasswordRequest req,
            Authentication auth) {
        settingsService.changePassword(auth.getName(), req);
    }

    // GET PREFERENCES
    @GetMapping("/preferences")
    public PreferencesRequest getPreferences(Authentication auth) {
        return settingsService.getPreferences(auth.getName());
    }

    // UPDATE PREFERENCES
    @PatchMapping("/preferences")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePreferences(
            @RequestBody PreferencesRequest req,
            Authentication auth) {
        settingsService.updatePreferences(auth.getName(), req);
    }

    // DELETE ACCOUNT
    @PostMapping("/delete-account")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(
            @RequestBody Map<String, String> body,
            Authentication auth) {
        settingsService.deleteAccount(
                auth.getName(),
                body.get("confirmEmail"));
    }

    // GET SESSIONS
    @GetMapping("/sessions")
    public List<SessionDTO> getSessions(HttpServletRequest request) {
        String token = extractToken(request);
        return settingsService.getSessions(token);
    }

    // REVOKE SESSION
    @DeleteMapping("/sessions/{jti}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeSession(
            @PathVariable String jti,
            HttpServletRequest request) {
        String token = extractToken(request);
        settingsService.revokeSession(jti, token);
    }

    // REVOKE ALL OTHER SESSIONS
    @DeleteMapping("/sessions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeAllOther(HttpServletRequest request) {
        String token = extractToken(request);
        settingsService.revokeAllOtherSessions(token);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        throw new RuntimeException("No token found");
    }
}