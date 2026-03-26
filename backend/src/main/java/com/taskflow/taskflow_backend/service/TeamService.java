package com.taskflow.taskflow_backend.service;

import com.taskflow.taskflow_backend.dto.MemberDTO;
import com.taskflow.taskflow_backend.dto.TaskResponse;
import com.taskflow.taskflow_backend.entity.*;
import com.taskflow.taskflow_backend.exception.ResourceNotFoundException;
import com.taskflow.taskflow_backend.exception.TaskAccessDeniedException;
import com.taskflow.taskflow_backend.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;  

    // ===============================
    // CREATE TEAM
    // ===============================

    public Team createTeam(Team team) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        User manager = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        team.setManager(manager);

        return teamRepository.save(team);
    }

    // ===============================
    // GET ALL TEAMS
    // ===============================

    public List<Team> getTeams() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // ADMIN → see all teams
        if (user.getRole() == Role.ADMIN) {
            return teamRepository.findAll();
        }

        // MANAGER → see teams they manage and they belong to
        if (user.getRole() == Role.MANAGER) {

                // Teams where manager is owner
                List<Team> managedTeams = teamRepository.findByManager_Id(user.getId());

                // Teams where manager is member
                List<Team> memberTeams = teamMemberRepository
                                .findByUser_Id(user.getId())
                                .stream()
                                .map(TeamMember::getTeam)
                                .toList();

                // Combine both (avoid duplicates)
                return Stream.concat(managedTeams.stream(), memberTeams.stream())
                                .distinct()
                                .toList();
        }

        // MEMBER / VIEWER → teams they belong to
        List<TeamMember> memberships = teamMemberRepository.findByUser_Id(user.getId());

        return memberships.stream()
                .map(TeamMember::getTeam)
                .toList();
    }

    // ===============================
    // GET TEAM BY ID
    // ===============================

    public Team getTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Team not found"));
    }

    // ===============================
    // DELETE TEAM
    // ===============================

    public void deleteTeam(Long id) {

        Team team = teamRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Team not found"));

        validateManagerAccess(team);
        // ✅ Delete members first then delete team
        teamMemberRepository.deleteAll(
                        teamMemberRepository.findByTeam_Id(id));

        teamRepository.delete(team);
    }

    // ===============================
    // GET TEAM MEMBERS
    // ===============================
    public List<MemberDTO> getMembers(Long teamId) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        return teamMemberRepository.findByTeam_Id(teamId)
                .stream()
                .map(tm -> new MemberDTO(
                        tm.getUser().getId(),
                        tm.getUser().getFullName(),
                        tm.getUser().getEmail(),
                        tm.getUser().getRole().name(),
                        tm.getUser().getIsActive()))
                .toList();
    }

    // ===============================
    // ADD MEMBER
    // ===============================

    public void addMember(Long teamId, Long userId) {

        Team team = getTeam(teamId);

        validateManagerAccess(team);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        TeamMemberId id = new TeamMemberId(teamId, userId);

        if (teamMemberRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already in team");
        }

        TeamMember member = TeamMember.builder()
                .id(id)
                .team(team)
                .user(user)
                .build();

        teamMemberRepository.save(member);
    }

    // ===============================
    // REMOVE MEMBER
    // ===============================

    public void removeMember(Long teamId, Long userId) {

        Team team = getTeam(teamId);

        validateManagerAccess(team);

        TeamMemberId id = new TeamMemberId(teamId, userId);

        teamMemberRepository.deleteById(id);
    }

    // ===============================
    // GET TEAM TASKS
    // ===============================
    public List<TaskResponse> getTeamTasks(Long teamId, String email) {

            User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            teamRepository.findById(teamId)
                            .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

            // ADMIN/MANAGER see all — MEMBER/VIEWER must be a team member
            boolean isAdminOrManager = user.getRole() == Role.ADMIN ||
                            user.getRole() == Role.MANAGER;

            boolean isMember = teamMemberRepository.findByTeam_Id(teamId)
                            .stream()
                            .anyMatch(tm -> tm.getUser().getId().equals(user.getId()));

            if (!isAdminOrManager && !isMember) {
                    throw new TaskAccessDeniedException(
                                    "You are not a member of this team");
            }

            return taskRepository.findByTeam_Id(teamId)
                            .stream()
                            .map(this::mapToTaskResponse)
                            .toList();
    }

    // ===============================
    // PRIVATE — Task mapper
    // ===============================
    private TaskResponse mapToTaskResponse(Task task) {
            return new TaskResponse(
                            task.getId(),
                            task.getTitle(),
                            task.getDescription(),
                            task.getDueDate(),
                            task.getStatus(),
                            task.getPriority(),
                            task.getCreatedAt(),
                            task.getUpdatedAt(),
                            task.getUser().getId(),
                            task.getAssignedTo() != null ? task.getAssignedTo().getId() : null,
                            task.getAssignedTo() != null ? task.getAssignedTo().getFullName() : null,
                            task.getTeam() != null ? task.getTeam().getId() : null,
                            task.getTeam() != null ? task.getTeam().getName() : null);
    }

    // ===============================
    // VALIDATE MANAGER ACCESS
    // ===============================

    private void validateManagerAccess(Team team) {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (user.getRole() == Role.ADMIN) return;

        if (user.getRole() == Role.MANAGER &&
                team.getManager().getId().equals(user.getId())) return;

        throw new TaskAccessDeniedException("Access denied");
    }
}