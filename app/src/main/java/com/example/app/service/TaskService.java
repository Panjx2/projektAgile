package com.example.app.service;

import com.example.app.data.*;
import com.example.app.repository.ProjectRepository;
import com.example.app.repository.TaskRepository;
import com.example.app.repository.UserRepository;
import com.example.app.specification.TaskSpecification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public Task createTask(Long projectId, Task task) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow();

        task.setProject(project);

        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }

        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.MEDIUM);
        }

        return taskRepository.save(task);
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow();
    }

    public Page<Task> getTasksByProject(Long projectId,
                                        String name,
                                        TaskStatus status,
                                        String username,
                                        TaskPriority priority,
                                        Pageable pageable) {

        Specification<Task> spec = Specification
                .where(TaskSpecification.hasProjectId(projectId))
                .and(TaskSpecification.hasName(name))
                .and(TaskSpecification.hasStatus(status))
                .and(TaskSpecification.hasPriority(priority))
                .and(TaskSpecification.hasAssignedUsername(username));

        return taskRepository.findAll(spec, pageable);
    }

    @Transactional
    public Task assignUser(Long taskId, Long userId) {

        Task task = taskRepository.findById(taskId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        task.setAssignedUser(user);

        return task;
    }

    public Task changeStatus(Long taskId, TaskStatus status) {

        Task task = getTaskById(taskId);
        task.setStatus(status);

        return taskRepository.save(task);
    }

    public Task changePriority(Long taskId, TaskPriority priority) {
        Task task = getTaskById(taskId);
        task.setPriority(priority);
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public Page<Task> getTasksByUser(Long userId,
                                     String name,
                                     TaskStatus status,
                                     TaskPriority priority,
                                     Pageable pageable) {

        Specification<Task> spec = Specification
                .where(TaskSpecification.hasUserId(userId))
                .and(TaskSpecification.hasName(name))
                .and(TaskSpecification.hasStatus(status))
                .and(TaskSpecification.hasPriority(priority));

        return taskRepository.findAll(spec, pageable);
    }
}