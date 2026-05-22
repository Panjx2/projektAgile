package com.project.controller;

import com.project.frontend.FrontendApplication;
import com.project.model.Task;
import com.project.model.TaskPriority;
import com.project.model.TaskStatus;
import com.project.model.User;
import com.project.service.FileService;
import com.project.service.TaskService;
import com.project.service.UserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(TaskController.class)
@ContextConfiguration(classes = FrontendApplication.class)
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private UserService userService;

    @MockBean
    private FileService fileService;

    @Test
    void taskListRendersView() throws Exception {
        when(taskService.getTasksByProject(1L)).thenReturn(List.of(sampleTask(11L)));
        when(userService.getAllUsers()).thenReturn(List.of(sampleUser(2L)));

        mockMvc.perform(get("/taskList").param("projectId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("taskList"))
                .andExpect(model().attributeExists("tasks", "projectId", "statuses", "priorities", "users"));
    }

    @Test
    void taskEditWithoutTaskIdCreatesNewTask() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(sampleUser(3L)));

        mockMvc.perform(get("/taskEdit").param("projectId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("taskEdit"))
                .andExpect(model().attributeExists("task", "users", "projectId", "statuses", "priorities"));
    }

    @Test
    void taskEditWithTaskIdLoadsTask() throws Exception {
        when(taskService.getTaskById(5L)).thenReturn(sampleTask(5L));
        when(fileService.getFilesByTask(5L)).thenReturn(List.of());
        when(userService.getAllUsers()).thenReturn(List.of(sampleUser(4L)));

        mockMvc.perform(get("/taskEdit")
                        .param("projectId", "1")
                        .param("taskId", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("taskEdit"))
                .andExpect(model().attributeExists("task", "taskFiles", "users", "projectId", "statuses", "priorities"));
    }

    @Test
    void taskEditSaveCreatesTaskAndAssignsUser() throws Exception {
        Task created = sampleTask(8L);
        when(taskService.createTask(eq(1L), any(Task.class))).thenReturn(created);

        mockMvc.perform(post("/taskEdit")
                        .param("projectId", "1")
                        .param("name", "New Task")
                        .param("priority", TaskPriority.LOW.name())
                        .param("status", TaskStatus.TODO.name())
                        .param("assignedUserId", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/taskList?projectId=1"));

        verify(taskService).createTask(eq(1L), any(Task.class));
        verify(taskService).assignUser(8L, 4L);
    }

    @Test
    void taskEditSaveUpdatesTask() throws Exception {
        Task updated = sampleTask(9L);
        when(taskService.changeStatus(9L, TaskStatus.IN_PROGRESS)).thenReturn(updated);
        when(taskService.changePriority(9L, TaskPriority.HIGH)).thenReturn(updated);

        mockMvc.perform(post("/taskEdit")
                        .param("projectId", "2")
                        .param("taskId", "9")
                        .param("name", "Task")
                        .param("priority", TaskPriority.HIGH.name())
                        .param("status", TaskStatus.IN_PROGRESS.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/taskList?projectId=2"));

        verify(taskService).changeStatus(9L, TaskStatus.IN_PROGRESS);
        verify(taskService).changePriority(9L, TaskPriority.HIGH);
    }

    @Test
    void taskEditCancelRedirects() throws Exception {
        mockMvc.perform(post("/taskEdit")
                        .param("cancel", "true")
                        .param("projectId", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/taskList?projectId=3"));
    }

    @Test
    void taskEditDeleteRedirects() throws Exception {
        mockMvc.perform(post("/taskEdit")
                        .param("delete", "true")
                        .param("projectId", "4")
                        .param("taskId", "12"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/taskList?projectId=4"));

        verify(taskService).deleteTask(12L);
    }

    private Task sampleTask(Long id) {
        Task task = new Task();
        task.setTaskId(id);
        task.setName("Task " + id);
        task.setPriority(TaskPriority.LOW);
        task.setStatus(TaskStatus.TODO);
        return task;
    }

    private User sampleUser(Long id) {
        User user = new User();
        user.setUser_id(id);
        user.setUsername("user" + id);
        return user;
    }
}
