package com.taskflow.taskflow_backend.repository;

import com.taskflow.taskflow_backend.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findTop20ByOrderByCreatedAtDesc();

}