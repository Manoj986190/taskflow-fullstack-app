package com.taskflow.taskflow_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.taskflow_backend.dto.LoginRequest;
import com.taskflow.taskflow_backend.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterAndLoginUser() throws Exception {

        // REGISTER
        RegisterRequest register = new RegisterRequest();
        register.setFullName("Test User");
        register.setEmail("test@example.com");
        register.setPassword("password123");
        register.setConfirmPassword("password123");   // IMPORTANT FIX

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());  // 201

        // LOGIN
        LoginRequest login = new LoginRequest();
        login.setEmail("test@example.com");
        login.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk());
    }
}