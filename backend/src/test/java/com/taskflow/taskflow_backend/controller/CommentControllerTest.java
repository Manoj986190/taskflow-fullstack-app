package com.taskflow.taskflow_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.taskflow_backend.dto.CreateCommentRequestDTO;
import com.taskflow.taskflow_backend.entity.Task;
import com.taskflow.taskflow_backend.entity.TaskStatus;
import com.taskflow.taskflow_backend.entity.User;
import com.taskflow.taskflow_backend.repository.CommentRepository;
import com.taskflow.taskflow_backend.repository.TaskRepository;
import com.taskflow.taskflow_backend.repository.UserRepository;
import com.taskflow.taskflow_backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private User testUser;
    private Task testTask;

    @BeforeEach
    void setup() {

        // Delete in correct order (child → parent)
        commentRepository.deleteAll();
        taskRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(
                User.builder()
                        .fullName("Test User")
                        .email("test@example.com")
                        .passwordHash("password")
                        .build());

        testTask = taskRepository.save(
                Task.builder()
                        .title("Test Task")
                        .description("Description")
                        .dueDate(LocalDate.now().plusDays(2))
                        .status(TaskStatus.TODO)
                        .user(testUser)
                        .build());

        token = jwtService.generateToken(testUser.getEmail(),testUser.getId(),testUser.getFullName(),testUser.getRole().name());
    }

    // =========================
    // ADD COMMENT
    // =========================
    @Test
    void addComment_shouldReturn201() throws Exception {

        CreateCommentRequestDTO request = new CreateCommentRequestDTO();
        request.setBody("This is a test comment");

        mockMvc.perform(post("/api/tasks/" + testTask.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.body").value("This is a test comment"))
                .andExpect(jsonPath("$.authorFullName").value("Test User"))
                .andExpect(jsonPath("$.isOwner").value(true));
    }

    // =========================
    // GET COMMENTS
    // =========================
    @Test
    void getComments_shouldReturnList() throws Exception {

        CreateCommentRequestDTO request = new CreateCommentRequestDTO();
        request.setBody("First Comment");

        mockMvc.perform(post("/api/tasks/" + testTask.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/tasks/" + testTask.getId() + "/comments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].body").value("First Comment"));
    }

    // =========================
    // DELETE COMMENT (OWNER)
    // =========================
    @Test
    void deleteComment_shouldReturn204_ifOwner() throws Exception {

        CreateCommentRequestDTO request = new CreateCommentRequestDTO();
        request.setBody("Delete Me");

        String response = mockMvc.perform(
                        post("/api/tasks/" + testTask.getId() + "/comments")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long commentId =
                objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/comments/" + commentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    // =========================
    // DELETE COMMENT (NOT OWNER)
    // =========================
    @Test
    void deleteComment_shouldReturn403_ifNotOwner() throws Exception {

        CreateCommentRequestDTO request = new CreateCommentRequestDTO();
        request.setBody("Owner Comment");

        String response = mockMvc.perform(
                        post("/api/tasks/" + testTask.getId() + "/comments")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long commentId =
                objectMapper.readTree(response).get("id").asLong();

        // Create another user
        User anotherUser = userRepository.save(
                User.builder()
                        .fullName("Another User")
                        .email("another@example.com")
                        .passwordHash("password")
                        .build()
        );

        String anotherToken =
                jwtService.generateToken(anotherUser.getEmail(),anotherUser.getId(),anotherUser.getFullName(),anotherUser.getRole().name());

        mockMvc.perform(delete("/api/comments/" + commentId)
                        .header("Authorization", "Bearer " + anotherToken))
                .andExpect(status().isForbidden());
    }
}
