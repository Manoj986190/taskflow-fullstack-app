package com.taskflow.taskflow_backend.controller;

import com.taskflow.taskflow_backend.dto.MemberDTO;
import com.taskflow.taskflow_backend.dto.TaskResponse;
import com.taskflow.taskflow_backend.entity.Team;
import com.taskflow.taskflow_backend.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    // ===============================
    // CREATE TEAM
    // ===============================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Team createTeam(@RequestBody Team team) {
        return teamService.createTeam(team);
    }

    // ===============================
    // GET ALL TEAMS
    // ===============================

    @GetMapping
    public List<Team> getTeams() {
        return teamService.getTeams();
    }

    // ===============================
    // GET TEAM BY ID
    // ===============================

    @GetMapping("/{id}")
    public Team getTeam(@PathVariable Long id) {
        return teamService.getTeam(id);
    }

    // ===============================
    // ADD MEMBER
    // ===============================

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void addMember(
            @PathVariable Long id,
            @RequestParam Long userId) {

        teamService.addMember(id, userId);
    }

    // ===============================
    // REMOVE MEMBER
    // ===============================

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void removeMember(
            @PathVariable Long id,
            @PathVariable Long userId) {

        teamService.removeMember(id, userId);
    }

    // ===============================
    // GET TEAM MEMBERS
    // ===============================
    @GetMapping("/{id}/members")
    public List<MemberDTO> getMembers(@PathVariable Long id) {
        return teamService.getMembers(id);
    }

    // ===============================
    // DELETE TEAM
    // ===============================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
    }

    // ===============================
    // GET TEAM TASKS
    // ===============================
    @GetMapping("/{id}/tasks")
    public List<TaskResponse> getTeamTasks(
            @PathVariable Long id,
            Authentication authentication) {
        return teamService.getTeamTasks(id, authentication.getName());
    }
}