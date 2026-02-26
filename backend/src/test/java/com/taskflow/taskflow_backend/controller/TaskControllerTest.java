package com.taskflow.taskflow_backend.controller;

import com.taskflow.taskflow_backend.entity.User;
import com.taskflow.taskflow_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "task@test.com")
    void shouldReturnTasksEndpoint() throws Exception {

        // Create user in DB
        User user = User.builder()
                .fullName("Task User")
                .email("task@test.com")
                .passwordHash("hash")
                .build();

        userRepository.save(user);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk());
    }
}