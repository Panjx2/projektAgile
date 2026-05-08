package com.project.controller;

import com.project.service.ProjectService;
import com.project.service.TaskService;
import com.project.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.project.exception.HttpException;
import org.apache.logging.log4j.util.Strings;
import com.project.model.Project;

@Controller
public class ProjectController {
    private final ProjectService projectService;
    private final UserService userService;
    private final TaskService taskService;

    public ProjectController(ProjectService projectService, UserService userService, TaskService taskService) {
        this.projectService = projectService;
        this.userService = userService;
        this.taskService = taskService;
    }

    @GetMapping({"/", "/projectList", "/projectList"})
    public String projectList(Model model) {
        model.addAttribute("projects", projectService.getAllProjects());
        return "projectList";
    }

    @GetMapping("/projectDetails")
    public String projectDetails(@RequestParam(name = "projectId", required = false) Long projectId, Model model) {
        if(projectId != null) {
            model.addAttribute("project", projectService.getProjectById(projectId));
            model.addAttribute("tasks",taskService.getTasksByProject(projectId));
            model.addAttribute("users",userService.getAllUsers());
        } else {
            return "redirect:/projectList";
        }
        return "projectDetails";
    }

    @GetMapping("/projectEdit")
    public String projectEdit(@RequestParam(name = "projectId", required = false) Long projectId, Model model) {
        if(projectId != null) {
            model.addAttribute("project", projectService.getProjectById(projectId));
        } else {
            model.addAttribute("project", new Project());
        }
        return "projectEdit";
    }

    @PostMapping(path = "/projectEdit")
    public String projectEditSave(@ModelAttribute @Valid Project project, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "projectEdit";
        }
        try {
            if (project.getProjectId() == null) {
                projectService.createProject(project);
            } else {
                projectService.updateProject(project.getProjectId(), project);
            }
        } catch (HttpException e) {
            bindingResult.rejectValue(Strings.EMPTY, "http.error", e.getMessage());
            return "projectEdit";
        }
        return "redirect:/projectList";
    }

    @PostMapping(params="cancel", path = "/projectEdit")
    public String projectEditCancel() {
        return "redirect:/projectList";
    }

    @PostMapping(params="delete", path = "/projectEdit")
    public String projectEditDelete(@ModelAttribute Project project) {
        projectService.deleteProject(project.getProjectId());
        return "redirect:/projectList";
    }

    @GetMapping("/projectUsers")
    public String projectUsers(@RequestParam(name = "projectId", required = false) Long projectId, Model model) {
        if (projectId == null) {
            return "redirect:/projectList";
        }
        populateProjectUsers(model, projectId);
        return "projectUsers";
    }

    @PostMapping(params = "addUser", path = "/projectUsers")
    public String projectUsersAdd(@RequestParam Long projectId,
                                  @RequestParam(required = false) Long userId,
                                  Model model) {
        if (userId == null) {
            populateProjectUsers(model, projectId);
            model.addAttribute("error", "Wybierz uzytkownika do dodania.");
            return "projectUsers";
        }
        try {
            projectService.addUserToProject(projectId, userId);
        } catch (HttpException e) {
            populateProjectUsers(model, projectId);
            model.addAttribute("error", e.getMessage());
            return "projectUsers";
        }
        return "redirect:/projectUsers?projectId=" + projectId;
    }

    @PostMapping(params = "removeUser", path = "/projectUsers")
    public String projectUsersRemove(@RequestParam Long projectId,
                                     @RequestParam Long userId,
                                     Model model) {
        try {
            projectService.removeUserFromProject(projectId, userId);
        } catch (HttpException e) {
            populateProjectUsers(model, projectId);
            model.addAttribute("error", e.getMessage());
            return "projectUsers";
        }
        return "redirect:/projectUsers?projectId=" + projectId;
    }

    private void populateProjectUsers(Model model, Long projectId) {
        model.addAttribute("project", projectService.getProjectById(projectId));
        model.addAttribute("users", userService.getAllUsers());
    }
}
