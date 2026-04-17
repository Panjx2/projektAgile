package com.example.app.controller;

import com.example.app.data.Task;
import com.example.app.data.TaskStatus;
import com.example.app.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/project/{projectId}")
    public Task createTask(@PathVariable Long projectId,
                           @RequestBody Task task) {
        return taskService.createTask(projectId, task);
    }

    @GetMapping("/{id}")
    public Task getTask(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @GetMapping("/project/{projectId}")
    public Page<Task> getTasksByProject(@PathVariable Long projectId,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false) TaskStatus status,
                                        @RequestParam(required = false) String username,
                                        Pageable pageable) {
        return taskService.getTasksByProject(projectId, name, status, username, pageable);
    }

    @GetMapping("/user/{userId}")
    public Page<Task> getTasksByUser(
            @PathVariable Long userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) TaskStatus status,
            Pageable pageable) {

        return taskService.getTasksByUser(userId, name, status, pageable);
    }

    @PatchMapping("/{taskId}/assign/{userId}")
    public Task assignUser(@PathVariable Long taskId,
                           @PathVariable Long userId) {
        return taskService.assignUser(taskId, userId);
    }

    @PatchMapping("/{taskId}/status")
    public Task changeStatus(@PathVariable Long taskId,
                             @RequestParam TaskStatus status) {
        return taskService.changeStatus(taskId, status);
    }

    @DeleteMapping("/{taskId}")
    public void deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
    }
}