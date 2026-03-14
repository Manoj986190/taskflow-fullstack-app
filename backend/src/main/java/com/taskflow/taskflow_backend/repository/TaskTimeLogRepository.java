package com.taskflow.taskflow_backend.repository;

import com.taskflow.taskflow_backend.entity.TaskTimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskTimeLogRepository
        extends JpaRepository<TaskTimeLog, Long> {

    List<TaskTimeLog> findByTask_IdOrderByLogDateDesc(Long taskId);

    @Query("SELECT COALESCE(SUM(t.durationMinutes), 0) " +
           "FROM TaskTimeLog t WHERE t.task.id = :taskId")
    int sumDurationByTaskId(@Param("taskId") Long taskId);
}