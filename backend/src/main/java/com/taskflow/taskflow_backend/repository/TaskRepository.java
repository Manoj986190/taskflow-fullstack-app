package com.taskflow.taskflow_backend.repository;

import com.taskflow.taskflow_backend.entity.Task;
import com.taskflow.taskflow_backend.entity.TaskPriority;
import com.taskflow.taskflow_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Tasks created by user
    List<Task> findByUser(User user);

    // Tasks created by user OR assigned to user
    List<Task> findByUserOrAssignedTo(User user, User assignedTo);
    List<Task> findByPriority(TaskPriority priority);

    Optional<Task> findByIdAndUser(Long id, User user);
    List<Task> findByTeam_Id(Long teamId);  // ✅ ADD

    // ✅ For MEMBER/VIEWER — own or assigned tasks
    List<Task> findByUser_IdOrAssignedTo_Id(
                    Long userId, Long assignedToId);

    // ✅ With priority filter
    @Query("SELECT t FROM Task t WHERE " +
                    "(t.user.id = :userId OR t.assignedTo.id = :assignedToId) " +
                    "AND t.priority = :priority")
    List<Task> findByUser_IdOrAssignedTo_IdAndPriority(
                    @Param("userId") Long userId,
                    @Param("assignedToId") Long assignedToId,
                    @Param("priority") TaskPriority priority);

    @Query("""
                SELECT t FROM Task t
                WHERE (t.user = :user OR t.assignedTo = :user)
                AND t.priority = :priority
            """)
    List<Task> findTasksByUserOrAssignedAndPriority(
            @Param("user") User user,
            @Param("priority") TaskPriority priority);

    // =========================================================
    // SUMMARY QUERIES — all DB-level, no in-memory computation
    // =========================================================
    @Query("""
            SELECT COUNT(t)
            FROM Task t
            WHERE t.user = :user OR t.assignedTo = :user
            """)
    int countTotalTasks(User user);

    //status counts
    @Query("""
            SELECT COUNT(t)
            FROM Task t
            WHERE (t.user = :user OR t.assignedTo = :user)
            AND t.status = 'TODO'
            """)
    int countTodo(User user);
    
    @Query("""
            SELECT COUNT(t)
            FROM Task t
            WHERE (t.user = :user OR t.assignedTo = :user)
            AND t.status = 'IN_PROGRESS'
            """)
    int countInProgress(User user);
    
    @Query("""
            SELECT COUNT(t)
            FROM Task t
            WHERE (t.user = :user OR t.assignedTo = :user)
            AND t.status = 'DONE'
            """)
    int countDone(User user);

    //priority counts
    @Query("""
            SELECT COUNT(t)
            FROM Task t
            WHERE (t.user = :user OR t.assignedTo = :user)
            AND t.priority = 'HIGH'
            """)
    int countHigh(User user);
    
    @Query("""
            SELECT COUNT(t)
            FROM Task t
            WHERE (t.user = :user OR t.assignedTo = :user)
            AND t.priority = 'MEDIUM'
            """)
    int countMedium(User user);
    
    @Query("""
            SELECT COUNT(t)
            FROM Task t
            WHERE (t.user = :user OR t.assignedTo = :user)
            AND t.priority = 'LOW'
            """)
    int countLow(User user);

    //overdue count
    @Query("""
            SELECT COUNT(t)
            FROM Task t
            WHERE (t.user = :user OR t.assignedTo = :user)
            AND t.dueDate < CURRENT_DATE
            AND t.status != 'DONE'
            """)
    int countOverdue(User user);

    //tasks this week
    @Query(value = """
            SELECT COUNT(*)
            FROM tasks t
            WHERE (t.user_id = :#{#user.id} OR t.assigned_to = :#{#user.id})
            AND t.created_at >= CURRENT_DATE - INTERVAL '7 days'
            """, nativeQuery = true)
    int countTasksThisWeek(User user);

    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.assignedTo = null WHERE t.assignedTo.id = :userId")
    void clearAssignedTo(@Param("userId") Long userId);
    
}