package com.example.app;

import com.example.app.controller.ProjectController;
import com.example.app.dto.ProjectDto;
import com.example.app.data.Project;
import com.example.app.mapper.DtoMapper;
import com.example.app.service.ProjectService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@WithMockUser(roles = "USER")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private DtoMapper dtoMapper;

    @Test
    void shouldReturn400ForIllegalArgumentException() throws Exception {
        when(dtoMapper.toEntity(isA(ProjectDto.class))).thenReturn(new Project());
        when(projectService.createProject(any()))
                .thenThrow(new IllegalArgumentException("Nieprawidlowe dane wejsciowe"));

        mockMvc.perform(post("/api/projects").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nieprawidlowe dane wejsciowe"));
    }

    @Test
    void shouldReturn404ForEntityNotFoundException() throws Exception {
        when(projectService.getProjectById(1L))
                .thenThrow(new EntityNotFoundException("Nie znaleziono projektu"));

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Nie znaleziono projektu"));
    }

    @Test
    void shouldReturn400WithExceptionMessage() throws Exception {
        when(dtoMapper.toEntity(isA(ProjectDto.class))).thenReturn(new Project());
        when(projectService.createProject(any()))
                .thenThrow(new IllegalArgumentException("Blad walidacji: pole 'name' jest wymagane"));

        mockMvc.perform(post("/api/projects").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Blad walidacji: pole 'name' jest wymagane"));
    }

    @Test
    void shouldReturn404WithEntityNotFoundMessage() throws Exception {
        when(projectService.getProjectById(404L))
                .thenThrow(new EntityNotFoundException("Projekt o id 404 nie istnieje"));

        mockMvc.perform(get("/api/projects/404"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Projekt o id 404 nie istnieje"));
    }

    @Test
    void shouldReturnCorrectContentTypeForErrorResponse() throws Exception {
        when(dtoMapper.toEntity(isA(ProjectDto.class))).thenReturn(new Project());
        when(projectService.createProject(any()))
                .thenThrow(new IllegalArgumentException("Blad"));

        mockMvc.perform(post("/api/projects").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
    }

    @Test
    void shouldHandleMultipleExceptionTypesIndependently() throws Exception {
        when(projectService.getProjectById(1L))
                .thenThrow(new EntityNotFoundException("Nie znaleziono"));

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isNotFound());

        when(dtoMapper.toEntity(isA(ProjectDto.class))).thenReturn(new Project());
        when(projectService.createProject(any()))
                .thenThrow(new IllegalArgumentException("Nieprawidlowy input"));

        mockMvc.perform(post("/api/projects").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }
}
