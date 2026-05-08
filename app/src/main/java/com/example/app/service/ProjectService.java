package com.example.app.service;

import com.example.app.data.Project;
import com.example.app.data.User;
import com.example.app.repository.ProjectRepository;
import com.example.app.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow();
    }

    public Project updateProject(Long id, Project updated) {
        Project project = getProjectById(id);
        project.setName(updated.getName());
        return projectRepository.save(project);
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    @Transactional
    public Project addUserToProject(Long projectId, Long userId) {

        Project project = projectRepository.findById(projectId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        if (project.getUsers() == null) {
            project.setUsers(new HashSet<>());
        }

        if (user.getProjects() == null) {
            user.setProjects(new HashSet<>());
        }

        project.getUsers().add(user);
        user.getProjects().add(project);

        return project;
    }

    public Project removeUserFromProject(Long projectId, Long userId) {

        Project project = getProjectById(projectId);
        User user = userRepository.findById(userId).orElseThrow();

        project.getUsers().remove(user);

        return projectRepository.save(project);
    }


    public Page<Project> getProjects(String name, Pageable pageable) {

        if (name != null && !name.isEmpty()) {
            return projectRepository.findByNameContainingIgnoreCase(name, pageable);
        }

        return projectRepository.findAll(pageable);
    }
}
