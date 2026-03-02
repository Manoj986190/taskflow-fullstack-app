package com.taskflow.taskflow_backend.repository;

import com.taskflow.taskflow_backend.entity.Task;
import com.taskflow.taskflow_backend.entity.TaskPriority;
import com.taskflow.taskflow_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Tasks created by user
    List<Task> findByUser(User user);

    // Tasks created by user OR assigned to user
    List<Task> findByUserOrAssignedTo(User user, User assignedTo);

    Optional<Task> findByIdAndUser(Long id, User user);

    @Query("""
                SELECT t FROM Task t
                WHERE (t.user = :user OR t.assignedTo = :user)
                AND t.priority = :priority
            """)
    List<Task> findTasksByUserOrAssignedAndPriority(
            @Param("user") User user,
            @Param("priority") TaskPriority priority);
}