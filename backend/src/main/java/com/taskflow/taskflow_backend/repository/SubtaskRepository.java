package com.taskflow.taskflow_backend.repository;

import com.taskflow.taskflow_backend.entity.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubtaskRepository extends JpaRepository<Subtask, Long> {

    List<Subtask> findByTask_IdOrderByCreatedAtAsc(Long taskId);

    int countByTask_Id(Long taskId);

    @Query("SELECT COUNT(s) FROM Subtask s WHERE s.task.id = :taskId AND s.isComplete = true")
    int countCompletedByTaskId(@Param("taskId") Long taskId);
}