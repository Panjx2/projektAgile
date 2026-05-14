package com.project.service;

import com.project.model.Task;
import com.project.model.TaskPriority;
import com.project.model.TaskStatus;
import java.util.List;

public interface TaskService {
    Task createTask(Long projectId, Task task);

    Task getTaskById(Long id);

    List<Task> getTasksByProject(Long projectId);

    Task assignUser(Long taskId, Long userId);

    Task changeStatus(Long taskId, TaskStatus status);

    Task changePriority(Long taskId, TaskPriority priority);
    
    void deleteTask(Long taskId);
}