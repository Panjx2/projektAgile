package com.example.app.controller;

import com.example.app.data.Project;
import com.example.app.service.ProjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public Project createProject(@RequestBody Project project) {
        return projectService.createProject(project);
    }

    @GetMapping
    public List<Project> getAllProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/{id}")
    public Project getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id);
    }

    @PutMapping("/{id}")
    public Project updateProject(@PathVariable Long id, @RequestBody Project project) {
        return projectService.updateProject(id, project);
    }

    @DeleteMapping("/{id}")
    public void deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
    }

    @PostMapping("/{projectId}/users/{userId}")
    public Project addUserToProject(@PathVariable Long projectId,
                                    @PathVariable Long userId) {
        return projectService.addUserToProject(projectId, userId);
    }

    @DeleteMapping("/{projectId}/users/{userId}")
    public Project removeUserFromProject(@PathVariable Long projectId,
                                         @PathVariable Long userId) {
        return projectService.removeUserFromProject(projectId, userId);
    }
}