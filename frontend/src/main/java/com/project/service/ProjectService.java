package com.project.service;

import com.project.model.Project;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ProjectService {
    List<Project> getAllProjects();

    Page<Project> getProjects(String search, int page, int size);

    Project getProjectById(Long id);

    Project createProject(Project project);

    Project updateProject(Long id, Project project);

    void deleteProject(Long id);

    Project addUserToProject(Long projectId, Long userId);

    Project removeUserFromProject(Long projectId, Long userId);
}