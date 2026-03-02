package com.taskflow.taskflow_backend.service;

import com.taskflow.taskflow_backend.dto.TaskResponse;
import com.taskflow.taskflow_backend.entity.Task;
import com.taskflow.taskflow_backend.entity.TaskStatus;
import com.taskflow.taskflow_backend.entity.User;
import com.taskflow.taskflow_backend.repository.TaskRepository;
import com.taskflow.taskflow_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void shouldReturnTasksForUser() {

        // Arrange
        String email = "test@example.com";

        User user = User.builder()
                .id(1L)
                .email(email)
                .build();

        Task task = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .dueDate(LocalDate.now())
                .status(TaskStatus.TODO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user)
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(taskRepository.findByUser(user))
                .thenReturn(List.of(task));

        // Act
        List<TaskResponse> result =
                taskService.getTasksForUser(email, null);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Task", result.get(0).title());
        assertEquals(TaskStatus.TODO, result.get(0).status());
    }
}