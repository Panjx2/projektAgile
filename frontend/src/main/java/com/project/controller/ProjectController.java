package com.project.controller;

import com.project.service.ProjectService;
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

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/projectList")
    public String projectList(Model model) {
        model.addAttribute("projekty", projectService.getAllProjects());
        return "projectList";
    }

    @GetMapping("/projektDetails")
    public String projektDetails(@RequestParam(name = "projektId", required = false) Long projektId, Model model) {
        if(projektId != null) {
            model.addAttribute("projekt", projectService.getProjectById(projektId));
        } else {
            return "redirect:/projektList";
        }
        return "projektDetails";
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
}