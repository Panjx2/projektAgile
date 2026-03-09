package com.example.app.repository;

import com.example.app.data.Project;
import com.example.app.data.Task;
import com.example.app.data.TaskStatus;
import com.example.app.data.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByProject(Project project);
    List<Task> findByAssignedUser(User user);
    List<Task> findByProjectAndStatus(Project project, TaskStatus status);
}