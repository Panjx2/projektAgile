package com.example.app;

import com.example.app.controller.TaskController;
import com.example.app.data.Task;
import com.example.app.data.TaskPriority;
import com.example.app.data.TaskStatus;
import com.example.app.dto.TaskDto;
import com.example.app.mapper.DtoMapper;
import com.example.app.service.TaskService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@WithMockUser(roles = "USER")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private DtoMapper dtoMapper;

    private TaskDto buildDto(Long id, String name, TaskStatus status, TaskPriority priority) {
        TaskDto dto = new TaskDto();
        dto.setTaskId(id);
        dto.setName(name);
        dto.setStatus(status);
        dto.setPriority(priority);
        return dto;
    }

    @Test
    void shouldCreateTask() throws Exception {
        Task entity = new Task();
        TaskDto result = buildDto(1L, "Nowe zadanie", TaskStatus.TODO, TaskPriority.MEDIUM);
        result.setProjectId(10L);

        when(dtoMapper.toEntity(any(TaskDto.class))).thenReturn(entity);
        when(taskService.createTask(eq(10L), any(Task.class))).thenReturn(entity);
        when(dtoMapper.toDto(entity)).thenReturn(result);

        mockMvc.perform(post("/api/tasks/project/10").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nowe zadanie\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nowe zadanie"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));
    }

    @Test
    void shouldGetTaskById() throws Exception {
        Task entity = new Task();
        TaskDto dto = buildDto(5L, "Zadanie testowe", TaskStatus.IN_PROGRESS, TaskPriority.HIGH);

        when(taskService.getTaskById(5L)).thenReturn(entity);
        when(dtoMapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(get("/api/tasks/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.task_id").value(5))
                .andExpect(jsonPath("$.name").value("Zadanie testowe"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void shouldReturn404WhenTaskNotFound() throws Exception {
        when(taskService.getTaskById(99L)).thenThrow(new EntityNotFoundException("Zadanie nie znalezione"));

        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Zadanie nie znalezione"));
    }

    @Test
    void shouldGetTasksByProject() throws Exception {
        Task t1 = new Task();
        t1.setId(1L);
        Task t2 = new Task();
        t2.setId(2L);

        TaskDto dto1 = buildDto(1L, "Zadanie 1", TaskStatus.TODO, TaskPriority.LOW);
        TaskDto dto2 = buildDto(2L, "Zadanie 2", TaskStatus.DONE, TaskPriority.HIGH);

        var page = new PageImpl<>(List.of(t1, t2), PageRequest.of(0, 10), 2);

        when(taskService.getTasksByProject(eq(1L), any(), any(), any(), any(), any()))
                .thenReturn(page);
        when(dtoMapper.toDto(t1)).thenReturn(dto1);
        when(dtoMapper.toDto(t2)).thenReturn(dto2);

        mockMvc.perform(get("/api/tasks/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Zadanie 1"))
                .andExpect(jsonPath("$.content[1].name").value("Zadanie 2"));
    }

    @Test
    void shouldGetTasksByProjectFilteredByStatus() throws Exception {
        Task t = new Task();
        TaskDto dto = buildDto(3L, "Zadanie Done", TaskStatus.DONE, TaskPriority.MEDIUM);
        var page = new PageImpl<>(List.of(t));

        when(taskService.getTasksByProject(eq(1L), any(), eq(TaskStatus.DONE), any(), any(), any()))
                .thenReturn(page);
        when(dtoMapper.toDto(t)).thenReturn(dto);

        mockMvc.perform(get("/api/tasks/project/1").param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("DONE"));
    }

    @Test
    void shouldGetTasksByProjectFilteredByPriority() throws Exception {
        Task t = new Task();
        TaskDto dto = buildDto(4L, "High Priority", TaskStatus.TODO, TaskPriority.HIGH);
        var page = new PageImpl<>(List.of(t));

        when(taskService.getTasksByProject(eq(2L), any(), any(), any(), eq(TaskPriority.HIGH), any()))
                .thenReturn(page);
        when(dtoMapper.toDto(t)).thenReturn(dto);

        mockMvc.perform(get("/api/tasks/project/2").param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].priority").value("HIGH"));
    }

    @Test
    void shouldReturnEmptyPageWhenNoTasks() throws Exception {
        var emptyPage = new PageImpl<Task>(List.of());

        when(taskService.getTasksByProject(eq(1L), any(), any(), any(), any(), any()))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/tasks/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldAssignUserToTask() throws Exception {
        Task entity = new Task();
        TaskDto dto = buildDto(1L, "Przypisane zadanie", TaskStatus.TODO, TaskPriority.MEDIUM);
        dto.setAssignedUserId(5L);

        when(taskService.assignUser(1L, 5L)).thenReturn(entity);
        when(dtoMapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(patch("/api/tasks/1/assign/5").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedUserId").value(5));
    }

    @Test
    void shouldChangeTaskStatusToInProgress() throws Exception {
        Task entity = new Task();
        TaskDto dto = buildDto(1L, "Zadanie", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM);

        when(taskService.changeStatus(1L, TaskStatus.IN_PROGRESS)).thenReturn(entity);
        when(dtoMapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(patch("/api/tasks/1/status").with(csrf()).param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void shouldChangeTaskStatusToDone() throws Exception {
        Task entity = new Task();
        TaskDto dto = buildDto(1L, "Zadanie", TaskStatus.DONE, TaskPriority.MEDIUM);

        when(taskService.changeStatus(1L, TaskStatus.DONE)).thenReturn(entity);
        when(dtoMapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(patch("/api/tasks/1/status").with(csrf()).param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void shouldChangeTaskPriority() throws Exception {
        Task entity = new Task();
        TaskDto dto = buildDto(1L, "Zadanie", TaskStatus.TODO, TaskPriority.HIGH);

        when(taskService.changePriority(1L, TaskPriority.HIGH)).thenReturn(entity);
        when(dtoMapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(patch("/api/tasks/1/priority").with(csrf()).param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void shouldDeleteTask() throws Exception {
        doNothing().when(taskService).deleteTask(7L);

        mockMvc.perform(delete("/api/tasks/7").with(csrf()))
                .andExpect(status().isOk());

        verify(taskService).deleteTask(7L);
    }

    @Test
    void shouldGetTasksByProjectFilteredByName() throws Exception {
        Task t = new Task();
        TaskDto dto = buildDto(1L, "Fix bug", TaskStatus.TODO, TaskPriority.HIGH);
        var page = new PageImpl<>(List.of(t));

        when(taskService.getTasksByProject(eq(1L), eq("bug"), any(), any(), any(), any()))
                .thenReturn(page);
        when(dtoMapper.toDto(t)).thenReturn(dto);

        mockMvc.perform(get("/api/tasks/project/1").param("name", "bug"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Fix bug"));
    }

    @Test
    void shouldGetTasksByProjectFilteredByUsername() throws Exception {
        Task t = new Task();
        TaskDto dto = buildDto(2L, "Zadanie Jana", TaskStatus.TODO, TaskPriority.LOW);
        dto.setAssignedUserId(3L);
        var page = new PageImpl<>(List.of(t));

        when(taskService.getTasksByProject(eq(1L), any(), any(), eq("jan"), any(), any()))
                .thenReturn(page);
        when(dtoMapper.toDto(t)).thenReturn(dto);

        mockMvc.perform(get("/api/tasks/project/1").param("username", "jan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].assignedUserId").value(3));
    }

    @Test
    void shouldReturn400ForIllegalArgumentOnStatusChange() throws Exception {
        when(taskService.changeStatus(eq(1L), any())).thenThrow(new IllegalArgumentException("Nieznany status"));

        mockMvc.perform(patch("/api/tasks/1/status").with(csrf()).param("status", "IN_PROGRESS"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nieznany status"));
    }
}
