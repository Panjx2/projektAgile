package com.example.app;

import com.example.app.data.*;
import com.example.app.repository.*;
import com.example.app.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void shouldCreateTaskWithDefaultStatus() {
        Project project = new Project();
        Task task = new Task();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Task result = taskService.createTask(1L, task);

        assertEquals(TaskStatus.TODO, result.getStatus());
        assertEquals(project, result.getProject());
    }

    @Test
    void shouldAssignUserToTask() {
        Task task = new Task();
        User user = new User();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        Task result = taskService.assignUser(1L, 2L);

        assertEquals(user, result.getAssignedUser());
    }

    @Test
    void shouldChangeTaskStatus() {
        Task task = new Task();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.changeStatus(1L, TaskStatus.DONE);

        assertEquals(TaskStatus.DONE, result.getStatus());
    }

    @Test
    void shouldGetTasksByUser() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = new PageImpl<>(java.util.List.of(new Task()));
        when(taskRepository.findAll(
                ArgumentMatchers.<Specification<Task>>any(),
                eq(pageable)
        )).thenReturn(page);

        Page<Task> result = taskService.getTasksByUser(1L, null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldCreateTaskWithDefaultPriority() {
        Project project = new Project();
        Task task = new Task();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Task result = taskService.createTask(1L, task);

        assertEquals(TaskPriority.MEDIUM, result.getPriority());
    }

    @Test
    void shouldGetTaskById() {
        Task task = new Task();
        task.setName("Do something");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(1L);

        assertEquals("Do something", result.getName());
    }

    @Test
    void shouldThrowWhenTaskNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> taskService.getTaskById(99L));
    }

    @Test
    void shouldChangePriority() {
        Task task = new Task();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.changePriority(1L, TaskPriority.HIGH);

        assertEquals(TaskPriority.HIGH, result.getPriority());
    }

    @Test
    void shouldDeleteTask() {
        taskService.deleteTask(1L);

        verify(taskRepository).deleteById(1L);
    }

    @Test
    void shouldGetTasksByProject() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = new PageImpl<>(java.util.List.of(new Task(), new Task()));
        when(taskRepository.findAll(
                ArgumentMatchers.<Specification<Task>>any(),
                eq(pageable)
        )).thenReturn(page);

        Page<Task> result = taskService.getTasksByProject(1L, null, null, null, null, pageable);

        assertEquals(2, result.getTotalElements());
    }
}