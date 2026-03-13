package com.taskflow.taskflow_backend.repository;

import com.taskflow.taskflow_backend.entity.TeamMember;
import com.taskflow.taskflow_backend.entity.TeamMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMemberId> {

    List<TeamMember> findByUser_Id(Long userId);
    List<TeamMember> findByTeam_Id(Long teamId);

}