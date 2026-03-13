package com.taskflow.taskflow_backend.repository;

import com.taskflow.taskflow_backend.entity.ActivityLog;
import com.taskflow.taskflow_backend.entity.Task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findTop20ByOrderByCreatedAtDesc();
    
    @Transactional
    @Modifying
    void deleteByTask(Task task);

}