package com.taskflow.taskflow_backend.repository;

import com.taskflow.taskflow_backend.entity.Task;
import com.taskflow.taskflow_backend.entity.TaskStatus;
import com.taskflow.taskflow_backend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveTask() {

        // Create valid user (all NOT NULL fields set)
        User user = User.builder()
                .fullName("Test User")
                .email("repo@test.com")
                .passwordHash("dummy_hash")
                .build();

        user = userRepository.save(user);

        Task task = Task.builder()
                .title("Repo Test")
                .description("Repository Test Description")
                .dueDate(LocalDate.now())
                .status(TaskStatus.TODO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user)
                .build();

        repository.save(task);

        assertEquals(1, repository.findAll().size());
    }
}