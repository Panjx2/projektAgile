package com.example.app;

import com.example.app.data.User;
import com.example.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // === TESTY UWIERZYTELNIANIA ===

    @Test
    void shouldReturn401ForProjectsWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401ForTasksWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401ForUsersWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401ForCreateProjectWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAccessToProjectsWithValidCredentials() throws Exception {
        mockMvc.perform(get("/api/projects")
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn401WithWrongPassword() throws Exception {
        mockMvc.perform(get("/api/projects")
                        .with(httpBasic("admin", "wrongpassword")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowRegisterWithoutAuth() throws Exception {
        String userJson = """
                {
                  "username": "nowyuzer",
                  "email": "nowyuzer@test.com",
                  "firstName": "Nowy",
                  "lastName": "Uzer",
                  "password": "haslo123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("nowyuzer"));
    }

    @Test
    void shouldAllowAccessWithRegularUserCredentials() throws Exception {
        User user = new User();
        user.setUsername("regularuser");
        user.setEmail("regular@test.com");
        user.setFirstName("Regular");
        user.setLastName("User");
        user.setPassword(passwordEncoder.encode("userpass"));
        user.setRole("ROLE_USER");
        userRepository.save(user);

        mockMvc.perform(get("/api/projects")
                        .with(httpBasic("regularuser", "userpass")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn401ForDeleteWithoutAuth() throws Exception {
        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401ForPatchWithoutAuth() throws Exception {
        mockMvc.perform(patch("/api/tasks/1/status")
                        .param("status", "DONE"))
                .andExpect(status().isUnauthorized());
    }
}