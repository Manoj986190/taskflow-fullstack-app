package com.taskflow.taskflow_backend.service;

import com.taskflow.taskflow_backend.dto.*;
import com.taskflow.taskflow_backend.entity.*;
import com.taskflow.taskflow_backend.entity.UserPreferences.ThemePreference;
import com.taskflow.taskflow_backend.exception.ResourceNotFoundException;
import com.taskflow.taskflow_backend.repository.*;
import com.taskflow.taskflow_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final UserRepository userRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final TokenBlocklistRepository blocklistRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TaskRepository taskRepository;
    private final TeamMemberRepository teamMemberRepository;

    // ===============================
    // GET PROFILE
    // ===============================
    public UserProfileDTO getProfile(String email) {
        User user = getUser(email);
        UserPreferences prefs = getOrCreatePrefs(user);
        return new UserProfileDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                prefs.getAvatarColour() != null
                        ? prefs.getAvatarColour() : "#2563EB",
                prefs.getBio() != null ? prefs.getBio() : "");
    }

    // ===============================
    // UPDATE PROFILE (TC-ST01)
    // ===============================
    @Transactional
    public UserProfileDTO updateProfile(String email,
                                        UpdateProfileRequest req) {
        User user = getUser(email);
        UserPreferences prefs = getOrCreatePrefs(user);

        // ✅ Update fullName safely
        if (req.fullName() != null && !req.fullName().isBlank()) {
            user.setFullName(req.fullName().trim());
        }

        // ✅ Update avatarColour safely
        if (req.avatarColour() != null && !req.avatarColour().isBlank()) {
            prefs.setAvatarColour(req.avatarColour());
        }

        // ✅ Update bio — allow empty string to clear it
        prefs.setBio(req.bio() != null ? req.bio() : "");

        // ✅ Save both explicitly
        User savedUser = userRepository.save(user);
        UserPreferences savedPrefs = preferencesRepository.save(prefs);

        // ✅ Return fresh saved data
        return new UserProfileDTO(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                savedPrefs.getAvatarColour() != null
                        ? savedPrefs.getAvatarColour() : "#2563EB",
                savedPrefs.getBio() != null ? savedPrefs.getBio() : "");
    }

    // ===============================
    // CHANGE PASSWORD (TC-ST02)
    // ===============================
    @Transactional
    public void changePassword(String email,
                               ChangePasswordRequest req) {
        User user = getUser(email);

        // ✅ Validate current password
        if (!passwordEncoder.matches(req.currentPassword(),
                user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Current password is incorrect");
        }

        if (!req.newPassword().equals(req.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Passwords do not match");
        }

        if (req.newPassword().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must be at least 8 characters");
        }

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    // ===============================
    // UPDATE PREFERENCES (TC-ST06)
    // ===============================
    @Transactional
    public void updatePreferences(String email,
                                  PreferencesRequest req) {
        User user = getUser(email);
        UserPreferences prefs = getOrCreatePrefs(user);

        if (req.theme() != null) {
            try {
                prefs.setTheme(ThemePreference.valueOf(req.theme()));
            } catch (IllegalArgumentException e) {
                prefs.setTheme(ThemePreference.LIGHT); // ✅ fallback
            }
        }
        if (req.notifyAssigned() != null) {
            prefs.setNotifyAssigned(req.notifyAssigned());
        }
        if (req.notifyComment() != null) {
            prefs.setNotifyComment(req.notifyComment());
        }
        if (req.notifySubtask() != null) {
            prefs.setNotifySubtask(req.notifySubtask());
        }
        if (req.notifyOverdue() != null) {
            prefs.setNotifyOverdue(req.notifyOverdue());
        }
        if (req.notifyTeam() != null) {
            prefs.setNotifyTeam(req.notifyTeam());
        }

        preferencesRepository.save(prefs);
    }

    // ===============================
    // GET PREFERENCES
    // ===============================
    public PreferencesRequest getPreferences(String email) {
        User user = getUser(email);
        UserPreferences prefs = getOrCreatePrefs(user);
        return new PreferencesRequest(
                prefs.getTheme() != null
                        ? prefs.getTheme().name() : "LIGHT",
                prefs.getNotifyAssigned(),
                prefs.getNotifyComment(),
                prefs.getNotifySubtask(),
                prefs.getNotifyOverdue(),
                prefs.getNotifyTeam());
    }

    // ===============================
    // DELETE ACCOUNT (TC-ST08)
    // ===============================
    @Transactional
    public void deleteAccount(String email, String confirmEmail) {
        if (confirmEmail == null ||
                !email.equalsIgnoreCase(confirmEmail.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Email confirmation does not match");
        }

        User user = getUser(email);

        // ✅ Step 1 — clear assigned_to on tasks
        taskRepository.clearAssignedTo(user.getId());

        // ✅ Step 2 — remove from all teams
        teamMemberRepository.deleteByUser_Id(user.getId());

        // ✅ Step 3 — delete preferences
        preferencesRepository.findByUser_Id(user.getId())
                .ifPresent(preferencesRepository::delete);

        // ✅ Step 4 — delete blocklist entries
        blocklistRepository.deleteAll(
                blocklistRepository.findAllByUser_Id(user.getId()));

        // ✅ Step 5 — delete user
        userRepository.delete(user);
    }

    // ===============================
    // GET SESSIONS (TC-ST05)
    // ===============================
    public List<SessionDTO> getSessions(String token) {
        String jti = jwtService.extractJti(token);
        return List.of(new SessionDTO(
                jti,
                "Current Session",
                null,
                true));
    }

    // ===============================
    // REVOKE SESSION (TC-ST05)
    // ===============================
    @Transactional
    public void revokeSession(String jtiToRevoke,
                              String currentToken) {
        String email = jwtService.extractEmail(currentToken);
        User user = getUser(email);

        String currentJti = jwtService.extractJti(currentToken);
        if (jtiToRevoke.equals(currentJti)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot revoke your current session");
        }

        LocalDateTime expiresAt = jwtService
                .extractExpiration(currentToken)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        TokenBlocklist entry = TokenBlocklist.builder()
                .jti(jtiToRevoke)
                .user(user)
                .expiresAt(expiresAt)
                .build();

        blocklistRepository.save(entry);
    }

    // ===============================
    // REVOKE ALL OTHER SESSIONS
    // ===============================
    @Transactional
    public void revokeAllOtherSessions(String currentToken) {
        String email = jwtService.extractEmail(currentToken);
        User user = getUser(email);
        String currentJti = jwtService.extractJti(currentToken);
        blocklistRepository
                .deleteByUser_IdAndJtiNot(user.getId(), currentJti);
    }

    // ===============================
    // PRIVATE HELPERS
    // ===============================
    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    private UserPreferences getOrCreatePrefs(User user) {
        return preferencesRepository.findByUser_Id(user.getId())
                .orElseGet(() -> {
                    UserPreferences prefs = UserPreferences.builder()
                            .user(user)
                            .build();
                    return preferencesRepository.save(prefs);
                });
    }
}