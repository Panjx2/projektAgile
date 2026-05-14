package com.example.app.mapper;

import com.example.app.data.Project;
import com.example.app.data.Task;
import com.example.app.data.User;
import com.example.app.dto.ProjectDto;
import com.example.app.dto.TaskDto;
import com.example.app.dto.UserDto;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class DtoMapper {

    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole());
        return dto;
    }

    public User toEntity(UserDto dto) {
        User user = new User();
        user.setId(dto.getUserId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setRole(dto.getRole());
        user.setPassword(dto.getPassword());
        return user;
    }

    public ProjectDto toDto(Project project) {
        ProjectDto dto = new ProjectDto();
        dto.setProjectId(project.getId());
        dto.setName(project.getName());
        if (project.getUsers() != null) {
            dto.setUserIds(
                    project.getUsers()
                            .stream()
                            .map(User::getId)
                            .collect(Collectors.toSet())
            );
        }
        return dto;
    }

    public Project toEntity(ProjectDto dto) {
        Project project = new Project();
        project.setId(dto.getProjectId());
        project.setName(dto.getName());
        project.setUsers(new HashSet<>());
        return project;
    }

    public TaskDto toDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setTaskId(task.getId());
        dto.setName(task.getName());
        dto.setPriority(task.getPriority());
        dto.setStatus(task.getStatus());
        if (task.getAssignedUser() != null) {
            dto.setAssignedUserId(task.getAssignedUser().getId());
        }
        if (task.getProject() != null) {
            dto.setProjectId(task.getProject().getId());
        }
        return dto;
    }

    public Task toEntity(TaskDto dto) {
        Task task = new Task();
        task.setId(dto.getTaskId());
        task.setName(dto.getName());
        task.setPriority(dto.getPriority());
        task.setStatus(dto.getStatus());
        return task;
    }
}
