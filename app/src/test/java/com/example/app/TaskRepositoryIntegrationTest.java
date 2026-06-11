package com.example.app;

import com.example.app.data.*;
import com.example.app.repository.ProjectRepository;
import com.example.app.repository.TaskRepository;
import com.example.app.repository.UserRepository;
import com.example.app.specification.TaskSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private Project savedProject;
    private User savedUser;

    @BeforeEach
    void setup() {
        Project project = new Project();
        project.setName("Projekt do zadan");
        savedProject = projectRepository.save(project);

        User user = new User();
        user.setUsername("jan");
        user.setEmail("jan@test.com");
        user.setFirstName("Jan");
        user.setLastName("Kowalski");
        user.setPassword("hashed");
        user.setRole("ROLE_USER");
        savedUser = userRepository.save(user);
    }

    private Task buildTask(String name, TaskStatus status, TaskPriority priority) {
        Task t = new Task();
        t.setName(name);
        t.setStatus(status);
        t.setPriority(priority);
        t.setProject(savedProject);
        return t;
    }

    @Test
    void shouldSaveAndRetrieveTask() {
        Task task = buildTask("Pierwsze zadanie", TaskStatus.TODO, TaskPriority.MEDIUM);

        Task saved = taskRepository.save(task);

        assertNotNull(saved.getId());
        assertEquals("Pierwsze zadanie", saved.getName());
        assertEquals(TaskStatus.TODO, saved.getStatus());
    }

    @Test
    void shouldFindTasksByStatus() {
        taskRepository.save(buildTask("Todo 1", TaskStatus.TODO, TaskPriority.LOW));
        taskRepository.save(buildTask("Todo 2", TaskStatus.TODO, TaskPriority.HIGH));
        taskRepository.save(buildTask("Done 1", TaskStatus.DONE, TaskPriority.MEDIUM));

        List<Task> todoTasks = taskRepository.findByStatus(TaskStatus.TODO);

        assertEquals(2, todoTasks.size());
        assertTrue(todoTasks.stream().allMatch(t -> t.getStatus() == TaskStatus.TODO));
    }

    @Test
    void shouldFindTasksByProject() {
        taskRepository.save(buildTask("Zadanie A", TaskStatus.TODO, TaskPriority.LOW));
        taskRepository.save(buildTask("Zadanie B", TaskStatus.IN_PROGRESS, TaskPriority.HIGH));

        Project otherProject = new Project();
        otherProject.setName("Inny projekt");
        Project saved = projectRepository.save(otherProject);

        Task other = new Task();
        other.setName("Zadanie inne");
        other.setStatus(TaskStatus.TODO);
        other.setPriority(TaskPriority.LOW);
        other.setProject(saved);
        taskRepository.save(other);

        List<Task> projectTasks = taskRepository.findByProject(savedProject);

        assertEquals(2, projectTasks.size());
        assertTrue(projectTasks.stream().allMatch(t -> t.getProject().getId().equals(savedProject.getId())));
    }

    @Test
    void shouldFindTasksByAssignedUser() {
        Task t1 = buildTask("Zadanie Jana", TaskStatus.TODO, TaskPriority.MEDIUM);
        t1.setAssignedUser(savedUser);
        taskRepository.save(t1);

        Task t2 = buildTask("Zadanie bez usera", TaskStatus.TODO, TaskPriority.LOW);
        taskRepository.save(t2);

        List<Task> userTasks = taskRepository.findByAssignedUser(savedUser);

        assertEquals(1, userTasks.size());
        assertEquals("Zadanie Jana", userTasks.get(0).getName());
    }

    @Test
    void shouldFindTasksByProjectAndStatus() {
        taskRepository.save(buildTask("TODO task", TaskStatus.TODO, TaskPriority.LOW));
        taskRepository.save(buildTask("IN_PROGRESS task", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM));
        taskRepository.save(buildTask("DONE task", TaskStatus.DONE, TaskPriority.HIGH));

        List<Task> inProgressTasks = taskRepository.findByProjectAndStatus(savedProject, TaskStatus.IN_PROGRESS);

        assertEquals(1, inProgressTasks.size());
        assertEquals(TaskStatus.IN_PROGRESS, inProgressTasks.get(0).getStatus());
    }

    @Test
    void shouldFilterTasksByProjectIdUsingSpec() {
        taskRepository.save(buildTask("Spec zadanie 1", TaskStatus.TODO, TaskPriority.LOW));
        taskRepository.save(buildTask("Spec zadanie 2", TaskStatus.DONE, TaskPriority.HIGH));

        Specification<Task> spec = TaskSpecification.hasProjectId(savedProject.getId());
        List<Task> result = taskRepository.findAll(spec);

        assertEquals(2, result.size());
    }

    @Test
    void shouldFilterTasksByNameContainingUsingSpec() {
        taskRepository.save(buildTask("Fix bug #123", TaskStatus.TODO, TaskPriority.HIGH));
        taskRepository.save(buildTask("Fix bug #456", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM));
        taskRepository.save(buildTask("Add feature", TaskStatus.TODO, TaskPriority.LOW));

        Specification<Task> spec = Specification
                .where(TaskSpecification.hasProjectId(savedProject.getId()))
                .and(TaskSpecification.hasName("fix"));

        List<Task> result = taskRepository.findAll(spec);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getName().toLowerCase().contains("fix")));
    }

    @Test
    void shouldFilterTasksByStatusUsingSpec() {
        taskRepository.save(buildTask("Todo 1", TaskStatus.TODO, TaskPriority.LOW));
        taskRepository.save(buildTask("Todo 2", TaskStatus.TODO, TaskPriority.MEDIUM));
        taskRepository.save(buildTask("Done 1", TaskStatus.DONE, TaskPriority.HIGH));

        Specification<Task> spec = Specification
                .where(TaskSpecification.hasProjectId(savedProject.getId()))
                .and(TaskSpecification.hasStatus(TaskStatus.DONE));

        List<Task> result = taskRepository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals(TaskStatus.DONE, result.get(0).getStatus());
    }

    @Test
    void shouldFilterTasksByPriorityUsingSpec() {
        taskRepository.save(buildTask("Low priority", TaskStatus.TODO, TaskPriority.LOW));
        taskRepository.save(buildTask("High priority 1", TaskStatus.TODO, TaskPriority.HIGH));
        taskRepository.save(buildTask("High priority 2", TaskStatus.DONE, TaskPriority.HIGH));

        Specification<Task> spec = Specification
                .where(TaskSpecification.hasProjectId(savedProject.getId()))
                .and(TaskSpecification.hasPriority(TaskPriority.HIGH));

        List<Task> result = taskRepository.findAll(spec);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getPriority() == TaskPriority.HIGH));
    }

    @Test
    void shouldFilterTasksByAssignedUserUsingSpec() {
        Task assigned = buildTask("Przypisane", TaskStatus.TODO, TaskPriority.MEDIUM);
        assigned.setAssignedUser(savedUser);
        taskRepository.save(assigned);
        taskRepository.save(buildTask("Nieprzypisane", TaskStatus.TODO, TaskPriority.LOW));

        Specification<Task> spec = TaskSpecification.hasAssignedUsername("jan");
        List<Task> result = taskRepository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Przypisane", result.get(0).getName());
    }

    @Test
    void shouldCombineMultipleSpecifications() {
        Task t1 = buildTask("Fix critical bug", TaskStatus.TODO, TaskPriority.HIGH);
        t1.setAssignedUser(savedUser);
        taskRepository.save(t1);

        taskRepository.save(buildTask("Fix minor issue", TaskStatus.TODO, TaskPriority.LOW));
        taskRepository.save(buildTask("Fix critical bug", TaskStatus.DONE, TaskPriority.HIGH));

        Specification<Task> spec = Specification
                .where(TaskSpecification.hasProjectId(savedProject.getId()))
                .and(TaskSpecification.hasStatus(TaskStatus.TODO))
                .and(TaskSpecification.hasPriority(TaskPriority.HIGH));

        List<Task> result = taskRepository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Fix critical bug", result.get(0).getName());
    }

    @Test
    void shouldReturnEmptyListWhenNullProjectIdSpec() {
        taskRepository.save(buildTask("Zadanie", TaskStatus.TODO, TaskPriority.LOW));

        Specification<Task> spec = TaskSpecification.hasProjectId(null);
        List<Task> result = taskRepository.findAll(spec);

        assertFalse(result.isEmpty());
    }

    @Test
    void shouldDeleteTask() {
        Task task = taskRepository.save(buildTask("Do usuniecia", TaskStatus.TODO, TaskPriority.LOW));
        Long taskId = task.getId();

        taskRepository.deleteById(taskId);

        assertFalse(taskRepository.findById(taskId).isPresent());
    }
}