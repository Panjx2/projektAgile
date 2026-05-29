package com.example.app;

import com.example.app.data.*;
import com.example.app.dto.*;
import com.example.app.mapper.DtoMapper;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DtoMapperTest {

    private final DtoMapper mapper = new DtoMapper();

    @Test
    void shouldMapUserToDto() {
        User user = new User();
        user.setId(1L);
        user.setUsername("jan");
        user.setEmail("jan@example.com");
        user.setFirstName("Jan");
        user.setLastName("Kowalski");
        user.setRole("ROLE_USER");

        UserDto dto = mapper.toDto(user);

        assertEquals(1L, dto.getUserId());
        assertEquals("jan", dto.getUsername());
        assertEquals("jan@example.com", dto.getEmail());
        assertEquals("Jan", dto.getFirstName());
        assertEquals("Kowalski", dto.getLastName());
        assertEquals("ROLE_USER", dto.getRole());
    }

    @Test
    void shouldNotCopyPasswordToUserDto() {
        User user = new User();
        user.setId(1L);
        user.setPassword("tajnehaslo");

        UserDto dto = mapper.toDto(user);

        assertNull(dto.getPassword());
    }

    @Test
    void shouldMapUserDtoToEntityWithAllFields() {
        UserDto dto = new UserDto();
        dto.setUserId(2L);
        dto.setUsername("ania");
        dto.setEmail("ania@example.com");
        dto.setFirstName("Anna");
        dto.setLastName("Nowak");
        dto.setRole("ROLE_ADMIN");
        dto.setPassword("haslo123");

        User user = mapper.toEntity(dto);

        assertEquals(2L, user.getId());
        assertEquals("ania", user.getUsername());
        assertEquals("ania@example.com", user.getEmail());
        assertEquals("Anna", user.getFirstName());
        assertEquals("Nowak", user.getLastName());
        assertEquals("ROLE_ADMIN", user.getRole());
        assertEquals("haslo123", user.getPassword());
    }

    @Test
    void shouldMapProjectToDto() {
        Project project = new Project();
        project.setId(5L);
        project.setName("Projekt A");

        ProjectDto dto = mapper.toDto(project);

        assertEquals(5L, dto.getProjectId());
        assertEquals("Projekt A", dto.getName());
    }

    @Test
    void shouldMapProjectToDtoWithUserIds() {
        User u1 = new User();
        u1.setId(10L);
        User u2 = new User();
        u2.setId(20L);

        Project project = new Project();
        project.setId(1L);
        project.setName("Projekt");
        project.setUsers(Set.of(u1, u2));

        ProjectDto dto = mapper.toDto(project);

        assertEquals(2, dto.getUserIds().size());
        assertTrue(dto.getUserIds().contains(10L));
        assertTrue(dto.getUserIds().contains(20L));
    }

    @Test
    void shouldMapProjectToDtoWithNullUsersAsNullUserIds() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Brak uzytkownikow");
        project.setUsers(null);

        ProjectDto dto = mapper.toDto(project);

        assertNull(dto.getUserIds());
    }

    @Test
    void shouldMapProjectDtoToEntity() {
        ProjectDto dto = new ProjectDto();
        dto.setProjectId(3L);
        dto.setName("Nowy projekt");

        Project project = mapper.toEntity(dto);

        assertEquals(3L, project.getId());
        assertEquals("Nowy projekt", project.getName());
    }

    @Test
    void shouldMapProjectDtoToEntityWithEmptyNonNullUsersSet() {
        ProjectDto dto = new ProjectDto();
        dto.setProjectId(1L);
        dto.setName("Test");

        Project project = mapper.toEntity(dto);

        assertNotNull(project.getUsers());
        assertTrue(project.getUsers().isEmpty());
    }

    @Test
    void shouldMapTaskToDtoWithAllRelations() {
        User user = new User();
        user.setId(7L);
        Project project = new Project();
        project.setId(3L);

        Task task = new Task();
        task.setId(11L);
        task.setName("Zrob cos");
        task.setPriority(TaskPriority.HIGH);
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setAssignedUser(user);
        task.setProject(project);

        TaskDto dto = mapper.toDto(task);

        assertEquals(11L, dto.getTaskId());
        assertEquals("Zrob cos", dto.getName());
        assertEquals(TaskPriority.HIGH, dto.getPriority());
        assertEquals(TaskStatus.IN_PROGRESS, dto.getStatus());
        assertEquals(7L, dto.getAssignedUserId());
        assertEquals(3L, dto.getProjectId());
    }

    @Test
    void shouldMapTaskToDtoWithNullAssignedUserAsNullId() {
        Task task = new Task();
        task.setId(1L);
        task.setName("Zadanie");
        task.setAssignedUser(null);

        TaskDto dto = mapper.toDto(task);

        assertNull(dto.getAssignedUserId());
    }

    @Test
    void shouldMapTaskToDtoWithNullProjectAsNullId() {
        Task task = new Task();
        task.setId(1L);
        task.setName("Zadanie");
        task.setProject(null);

        TaskDto dto = mapper.toDto(task);

        assertNull(dto.getProjectId());
    }

    @Test
    void shouldMapTaskDtoToEntity() {
        TaskDto dto = new TaskDto();
        dto.setTaskId(9L);
        dto.setName("Zadanie DTO");
        dto.setPriority(TaskPriority.LOW);
        dto.setStatus(TaskStatus.DONE);

        Task task = mapper.toEntity(dto);

        assertEquals(9L, task.getId());
        assertEquals("Zadanie DTO", task.getName());
        assertEquals(TaskPriority.LOW, task.getPriority());
        assertEquals(TaskStatus.DONE, task.getStatus());
    }

    @Test
    void shouldMapProjectDtoToEntityIgnoringUserIds() {
        ProjectDto dto = new ProjectDto();
        dto.setProjectId(1L);
        dto.setName("Projekt");
        dto.setUserIds(Set.of(1L, 2L, 3L));

        Project project = mapper.toEntity(dto);

        assertTrue(project.getUsers().isEmpty());
    }
}