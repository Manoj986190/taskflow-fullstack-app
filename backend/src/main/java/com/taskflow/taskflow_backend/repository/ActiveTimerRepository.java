package com.taskflow.taskflow_backend.repository;

import com.taskflow.taskflow_backend.entity.ActiveTimer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActiveTimerRepository
        extends JpaRepository<ActiveTimer, Long> {

    Optional<ActiveTimer> findByTask_IdAndUser_Id(
            Long taskId, Long userId);

    Optional<ActiveTimer> findByTask_Id(Long taskId);
}