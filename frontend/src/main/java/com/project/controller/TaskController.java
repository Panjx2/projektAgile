package com.project.controller;

import com.project.exception.HttpException;
import com.project.model.Task;
import com.project.model.TaskStatus;
import com.project.service.TaskService;
import com.project.service.UserService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @GetMapping("/taskList")
    public String taskList(@RequestParam Long projectId, Model model) {
        model.addAttribute("tasks", taskService.getTasksByProject(projectId));
        model.addAttribute("projectId", projectId);
        model.addAttribute("statuses", TaskStatus.values());
        return "taskList";
    }

    @GetMapping("/taskEdit")
    public String taskEdit(@RequestParam(required = false) Long taskId,
                           @RequestParam Long projectId,
                           Model model) {
        if (taskId != null) {
            model.addAttribute("task", taskService.getTaskById(taskId));
        } else {
            model.addAttribute("task", new Task());
        }
        populateTaskForm(model, projectId);
        return "taskEdit";
    }

    @PostMapping("/taskEdit")
    public String taskEditSave(@ModelAttribute Task task,
                               BindingResult bindingResult,
                               @RequestParam Long projectId,
                               @RequestParam(required = false) Long assignedUserId,
                               Model model) {
        if (bindingResult.hasErrors()) {
            populateTaskForm(model, projectId);
            return "taskEdit";
        }
        try {
            Task savedTask;
            if (task.getTaskId() == null) {
                savedTask = taskService.createTask(projectId, task);
            } else {
                savedTask = taskService.changeStatus(task.getTaskId(), task.getStatus());
            }
            if (assignedUserId != null) {
                taskService.assignUser(savedTask.getTaskId(), assignedUserId);
            }
        } catch (HttpException e) {
            bindingResult.rejectValue(Strings.EMPTY, "http.error", e.getMessage());
            populateTaskForm(model, projectId);
            return "taskEdit";
        }
        return "redirect:/taskList?projectId=" + projectId;
    }

    @PostMapping(params = "cancel", path = "/taskEdit")
    public String taskEditCancel(@RequestParam Long projectId) {
        return "redirect:/taskList?projectId=" + projectId;
    }

    @PostMapping(params = "delete", path = "/taskEdit")
    public String taskEditDelete(@ModelAttribute Task task, @RequestParam Long projectId) {
        taskService.deleteTask(task.getTaskId());
        return "redirect:/taskList?projectId=" + projectId;
    }

    private void populateTaskForm(Model model, Long projectId) {
        model.addAttribute("projectId", projectId);
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("users", userService.getAllUsers());
    }
}
