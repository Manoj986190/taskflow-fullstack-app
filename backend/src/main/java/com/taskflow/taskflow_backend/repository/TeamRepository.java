package com.taskflow.taskflow_backend.repository;

import com.taskflow.taskflow_backend.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByManager_Id(Long managerId);

}