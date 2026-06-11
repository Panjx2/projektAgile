package com.example.app;

import com.example.app.data.*;
import com.example.app.dto.ProjectDto;
import com.example.app.dto.TaskDto;
import com.example.app.dto.UserDto;
import com.example.app.repository.ProjectRepository;
import com.example.app.repository.TaskRepository;
import com.example.app.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ProjectWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setUsername("testmember");
        testUser.setEmail("testmember@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("Member");
        testUser.setPassword(passwordEncoder.encode("pass"));
        testUser.setRole("ROLE_USER");
        testUser = userRepository.save(testUser);
    }

    // === FLOW 1: Tworzenie projektu i pobranie listy ===

    @Test
    void shouldCreateProjectAndThenRetrieveIt() throws Exception {
        String projectJson = "{\"name\":\"Sprint 1\"}";

        MvcResult createResult = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sprint 1"))
                .andReturn();

        ProjectDto created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), ProjectDto.class);
        Long projectId = created.getProjectId();

        mockMvc.perform(get("/api/projects/" + projectId)
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sprint 1"))
                .andExpect(jsonPath("$.project_id").value(projectId));
    }

    @Test
    void shouldListAllProjectsAfterCreation() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Projekt Alpha\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/projects")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Projekt Beta\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/projects")
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // === FLOW 2: Dodawanie czlonkow do projektu ===

    @Test
    void shouldAddMemberToProjectAndVerify() throws Exception {
        MvcResult projectResult = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Team Project\"}"))
                .andExpect(status().isOk())
                .andReturn();

        Long projectId = objectMapper.readValue(
                projectResult.getResponse().getContentAsString(), ProjectDto.class).getProjectId();

        mockMvc.perform(post("/api/projects/" + projectId + "/users/" + testUser.getId())
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.project_id").value(projectId));

        mockMvc.perform(get("/api/projects/" + projectId)
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userIds").isArray());
    }

    @Test
    void shouldRemoveMemberFromProject() throws Exception {
        MvcResult projectResult = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Projekt z czlonkami\"}"))
                .andExpect(status().isOk())
                .andReturn();

        Long projectId = objectMapper.readValue(
                projectResult.getResponse().getContentAsString(), ProjectDto.class).getProjectId();

        mockMvc.perform(post("/api/projects/" + projectId + "/users/" + testUser.getId())
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/projects/" + projectId + "/users/" + testUser.getId())
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk());
    }

    // === FLOW 3: Cykl zycia zadania (TODO -> IN_PROGRESS -> DONE) ===

    @Test
    void shouldCreateTaskAndProgressThroughStatuses() throws Exception {
        MvcResult projResult = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Projekt Zadan\"}"))
                .andExpect(status().isOk())
                .andReturn();

        Long projectId = objectMapper.readValue(
                projResult.getResponse().getContentAsString(), ProjectDto.class).getProjectId();

        MvcResult taskResult = mockMvc.perform(post("/api/tasks/project/" + projectId)
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Implementacja loginu\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TODO"))
                .andReturn();

        Long taskId = objectMapper.readValue(
                taskResult.getResponse().getContentAsString(), TaskDto.class).getTaskId();

        mockMvc.perform(patch("/api/tasks/" + taskId + "/status")
                        .with(httpBasic("admin", "admin"))
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(patch("/api/tasks/" + taskId + "/status")
                        .with(httpBasic("admin", "admin"))
                        .param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void shouldCreateTaskWithDefaultPriorityAndStatus() throws Exception {
        MvcResult projResult = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Default Values Project\"}"))
                .andExpect(status().isOk())
                .andReturn();

        Long projectId = objectMapper.readValue(
                projResult.getResponse().getContentAsString(), ProjectDto.class).getProjectId();

        mockMvc.perform(post("/api/tasks/project/" + projectId)
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Zadanie bez defaults\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));
    }

    // === FLOW 4: Przypisywanie uzytkownika do zadania ===

    @Test
    void shouldAssignUserToTaskAndVerify() throws Exception {
        MvcResult projResult = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Assignment Project\"}"))
                .andExpect(status().isOk())
                .andReturn();

        Long projectId = objectMapper.readValue(
                projResult.getResponse().getContentAsString(), ProjectDto.class).getProjectId();

        MvcResult taskResult = mockMvc.perform(post("/api/tasks/project/" + projectId)
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Zadanie do przypisania\"}"))
                .andExpect(status().isOk())
                .andReturn();

        Long taskId = objectMapper.readValue(
                taskResult.getResponse().getContentAsString(), TaskDto.class).getTaskId();

        mockMvc.perform(patch("/api/tasks/" + taskId + "/assign/" + testUser.getId())
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedUserId").value(testUser.getId()));

        mockMvc.perform(get("/api/tasks/" + taskId)
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedUserId").value(testUser.getId()));
    }

    // === FLOW 5: Filtrowanie zadan ===

    @Test
    void shouldFilterTasksByStatusAfterCreation() throws Exception {
        MvcResult projResult = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Filter Project\"}"))
                .andExpect(status().isOk())
                .andReturn();

        Long projectId = objectMapper.readValue(
                projResult.getResponse().getContentAsString(), ProjectDto.class).getProjectId();

        MvcResult t1Result = mockMvc.perform(post("/api/tasks/project/" + projectId)
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Zadanie TODO\"}"))
                .andExpect(status().isOk())
                .andReturn();

        Long t1Id = objectMapper.readValue(t1Result.getResponse().getContentAsString(), TaskDto.class).getTaskId();

        mockMvc.perform(post("/api/tasks/project/" + projectId)
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Zadanie DONE\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/tasks/" + t1Id + "/status")
                        .with(httpBasic("admin", "admin"))
                        .param("status", "DONE"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks/project/" + projectId)
                        .with(httpBasic("admin", "admin"))
                        .param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    // === FLOW 6: Zmiana priorytetu ===

    @Test
    void shouldChangePriorityOfTask() throws Exception {
        MvcResult projResult = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Priority Project\"}"))
                .andExpect(status().isOk())
                .andReturn();

        Long projectId = objectMapper.readValue(
                projResult.getResponse().getContentAsString(), ProjectDto.class).getProjectId();

        MvcResult taskResult = mockMvc.perform(post("/api/tasks/project/" + projectId)
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Zadanie z priorytetem\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andReturn();

        Long taskId = objectMapper.readValue(
                taskResult.getResponse().getContentAsString(), TaskDto.class).getTaskId();

        mockMvc.perform(patch("/api/tasks/" + taskId + "/priority")
                        .with(httpBasic("admin", "admin"))
                        .param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    // === FLOW 7: Aktualizacja i usuwanie projektu ===

    @Test
    void shouldUpdateProjectNameAndVerify() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Stara Nazwa\"}"))
                .andExpect(status().isOk())
                .andReturn();

        Long projectId = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), ProjectDto.class).getProjectId();

        mockMvc.perform(put("/api/projects/" + projectId)
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nowa Nazwa\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nowa Nazwa"));

        mockMvc.perform(get("/api/projects/" + projectId)
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nowa Nazwa"));
    }

    @Test
    void shouldDeleteProjectAndVerifyGone() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Do Usuniecia\"}"))
                .andExpect(status().isOk())
                .andReturn();

        Long projectId = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), ProjectDto.class).getProjectId();

        mockMvc.perform(delete("/api/projects/" + projectId)
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk());

        assertFalse(projectRepository.findById(projectId).isPresent());
    }

    // === FLOW 8: Pobieranie danych uzytkownika ===

    @Test
    void shouldGetUserListAndThenUserDetails() throws Exception {
        mockMvc.perform(get("/api/users")
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/users/" + testUser.getId())
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testmember"))
                .andExpect(jsonPath("$.email").value("testmember@test.com"));
    }

    // === FLOW 9: Pelny sprint Agile ===

    @Test
    void shouldSimulateFullAgileSprint() throws Exception {
        MvcResult projResult = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sprint Q1 2025\"}"))
                .andExpect(status().isOk())
                .andReturn();

        Long projectId = objectMapper.readValue(
                projResult.getResponse().getContentAsString(), ProjectDto.class).getProjectId();

        mockMvc.perform(post("/api/projects/" + projectId + "/users/" + testUser.getId())
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk());

        String[] taskNames = {"Rejestracja uzytkownika", "Panel logowania", "Dashboard"};
        Long[] taskIds = new Long[3];

        for (int i = 0; i < taskNames.length; i++) {
            String taskJson = "{\"name\":\"" + taskNames[i] + "\"}";
            MvcResult tr = mockMvc.perform(post("/api/tasks/project/" + projectId)
                            .with(httpBasic("admin", "admin"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(taskJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("TODO"))
                    .andReturn();
            taskIds[i] = objectMapper.readValue(tr.getResponse().getContentAsString(), TaskDto.class).getTaskId();
        }

        mockMvc.perform(patch("/api/tasks/" + taskIds[0] + "/assign/" + testUser.getId())
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/tasks/" + taskIds[0] + "/status")
                        .with(httpBasic("admin", "admin"))
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/tasks/" + taskIds[0] + "/status")
                        .with(httpBasic("admin", "admin"))
                        .param("status", "DONE"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks/project/" + projectId)
                        .with(httpBasic("admin", "admin"))
                        .param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get("/api/tasks/project/" + projectId)
                        .with(httpBasic("admin", "admin"))
                        .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }
}
