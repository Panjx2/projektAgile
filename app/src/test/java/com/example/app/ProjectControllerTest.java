package com.example.app;

import com.example.app.controller.ProjectController;
import com.example.app.data.Project;
import com.example.app.dto.ProjectDto;
import com.example.app.mapper.DtoMapper;
import com.example.app.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import jakarta.persistence.EntityNotFoundException;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@WithMockUser(roles = "USER")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private DtoMapper dtoMapper;

    @Test
    void shouldCreateProject() throws Exception {
        Project entity = new Project();
        ProjectDto result = new ProjectDto();
        result.setProjectId(1L);
        result.setName("Nowy Projekt");

        when(dtoMapper.toEntity(any(ProjectDto.class))).thenReturn(entity);
        when(projectService.createProject(entity)).thenReturn(entity);
        when(dtoMapper.toDto(entity)).thenReturn(result);

        mockMvc.perform(post("/api/projects").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nowy Projekt\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nowy Projekt"))
                .andExpect(jsonPath("$.project_id").value(1));
    }

    @Test
    void shouldGetAllProjects() throws Exception {
        Project p1 = new Project();
        Project p2 = new Project();

        ProjectDto dto1 = new ProjectDto();
        dto1.setProjectId(1L);
        dto1.setName("Alpha");

        ProjectDto dto2 = new ProjectDto();
        dto2.setProjectId(2L);
        dto2.setName("Beta");

        when(projectService.getAllProjects()).thenReturn(List.of(p1, p2));
        when(dtoMapper.toDto(p1)).thenReturn(dto1);
        when(dtoMapper.toDto(p2)).thenReturn(dto2);

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Alpha"))
                .andExpect(jsonPath("$[1].name").value("Beta"));
    }

    @Test
    void shouldReturnEmptyListWhenNoProjects() throws Exception {
        when(projectService.getAllProjects()).thenReturn(List.of());

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldGetProjectById() throws Exception {
        Project entity = new Project();
        ProjectDto dto = new ProjectDto();
        dto.setProjectId(5L);
        dto.setName("Projekt Piatka");
        dto.setUserIds(Set.of(10L, 20L));

        when(projectService.getProjectById(5L)).thenReturn(entity);
        when(dtoMapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(get("/api/projects/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.project_id").value(5))
                .andExpect(jsonPath("$.name").value("Projekt Piatka"))
                .andExpect(jsonPath("$.userIds").isArray());
    }

    @Test
    void shouldReturn404WhenProjectNotFound() throws Exception {
        when(projectService.getProjectById(99L)).thenThrow(new EntityNotFoundException("Project not found"));

        mockMvc.perform(get("/api/projects/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Project not found"));
    }

    @Test
    void shouldUpdateProject() throws Exception {
        Project entity = new Project();
        ProjectDto result = new ProjectDto();
        result.setProjectId(1L);
        result.setName("Zmieniona Nazwa");

        when(dtoMapper.toEntity(any(ProjectDto.class))).thenReturn(entity);
        when(projectService.updateProject(eq(1L), any(Project.class))).thenReturn(entity);
        when(dtoMapper.toDto(entity)).thenReturn(result);

        mockMvc.perform(put("/api/projects/1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Zmieniona Nazwa\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Zmieniona Nazwa"));
    }

    @Test
    void shouldDeleteProject() throws Exception {
        doNothing().when(projectService).deleteProject(3L);

        mockMvc.perform(delete("/api/projects/3").with(csrf()))
                .andExpect(status().isOk());

        verify(projectService).deleteProject(3L);
    }

    @Test
    void shouldAddUserToProject() throws Exception {
        Project entity = new Project();
        ProjectDto dto = new ProjectDto();
        dto.setProjectId(1L);
        dto.setUserIds(Set.of(7L));

        when(projectService.addUserToProject(1L, 7L)).thenReturn(entity);
        when(dtoMapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(post("/api/projects/1/users/7").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.project_id").value(1));
    }

    @Test
    void shouldRemoveUserFromProject() throws Exception {
        Project entity = new Project();
        ProjectDto dto = new ProjectDto();
        dto.setProjectId(1L);
        dto.setUserIds(Set.of());

        when(projectService.removeUserFromProject(1L, 7L)).thenReturn(entity);
        when(dtoMapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(delete("/api/projects/1/users/7").with(csrf()))
                .andExpect(status().isOk());

        verify(projectService).removeUserFromProject(1L, 7L);
    }

    @Test
    void shouldReturnBadRequestForIllegalArgument() throws Exception {
        when(dtoMapper.toEntity(any(ProjectDto.class))).thenReturn(new Project());
        when(projectService.createProject(any())).thenThrow(new IllegalArgumentException("Nieprawidlowe dane"));

        mockMvc.perform(post("/api/projects").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"bad\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nieprawidlowe dane"));
    }

    @Test
    void shouldReturnProjectWithNullUserIds() throws Exception {
        Project entity = new Project();
        ProjectDto dto = new ProjectDto();
        dto.setProjectId(2L);
        dto.setName("Bez Uzytkownikow");
        dto.setUserIds(null);

        when(projectService.getProjectById(2L)).thenReturn(entity);
        when(dtoMapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(get("/api/projects/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.project_id").value(2));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentProject() throws Exception {
        when(dtoMapper.toEntity(any(ProjectDto.class))).thenReturn(new Project());
        when(projectService.updateProject(eq(999L), any())).thenThrow(new EntityNotFoundException("Projekt nie istnieje"));

        mockMvc.perform(put("/api/projects/999").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"updated\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Projekt nie istnieje"));
    }
}