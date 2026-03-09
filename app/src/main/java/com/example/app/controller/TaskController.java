package com.example.app.controller;

import com.example.app.data.Task;
import com.example.app.data.TaskStatus;
import com.example.app.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<Task> getTasksByProject(@PathVariable Long projectId) {
        return taskService.getTasksByProject(projectId);
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