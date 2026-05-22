package com.project.controller;

import com.project.frontend.FrontendApplication;
import com.project.model.FileInfo;
import com.project.model.Project;
import com.project.model.Task;
import com.project.model.User;
import com.project.service.FileService;
import com.project.service.ProjectService;
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

@WebMvcTest(ProjectController.class)
@ContextConfiguration(classes = FrontendApplication.class)
class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private UserService userService;

    @MockBean
    private TaskService taskService;

    @MockBean
    private FileService fileService;

    @Test
    void projectListRendersView() throws Exception {
        when(projectService.getAllProjects()).thenReturn(List.of(sampleProject(1L)));

        mockMvc.perform(get("/projectList"))
                .andExpect(status().isOk())
                .andExpect(view().name("projectList"))
                .andExpect(model().attributeExists("projects"));
    }

    @Test
    void projectDetailsWithProjectIdPopulatesModel() throws Exception {
        when(projectService.getProjectById(1L)).thenReturn(sampleProject(1L));
        when(taskService.getTasksByProject(1L)).thenReturn(List.of(sampleTask(10L)));
        when(userService.getAllUsers()).thenReturn(List.of(sampleUser(7L)));
        when(fileService.getFilesByProject(1L)).thenReturn(List.of(sampleFile(5L)));

        mockMvc.perform(get("/projectDetails").param("projectId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("projectDetails"))
                .andExpect(model().attributeExists("project", "tasks", "users", "files"));
    }

    @Test
    void projectDetailsWithoutProjectIdRedirects() throws Exception {
        mockMvc.perform(get("/projectDetails"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projectList"));
    }

    @Test
    void projectEditWithoutIdCreatesNewProject() throws Exception {
        mockMvc.perform(get("/projectEdit"))
                .andExpect(status().isOk())
                .andExpect(view().name("projectEdit"))
                .andExpect(model().attributeExists("project"));
    }

    @Test
    void projectEditWithIdLoadsProject() throws Exception {
        when(projectService.getProjectById(2L)).thenReturn(sampleProject(2L));

        mockMvc.perform(get("/projectEdit").param("projectId", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("projectEdit"))
                .andExpect(model().attributeExists("project"));
    }

    @Test
    void projectEditSaveCreatesProject() throws Exception {
        when(projectService.createProject(any(Project.class))).thenReturn(sampleProject(3L));

        mockMvc.perform(post("/projectEdit")
                        .param("name", "New Project"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projectList"));

        verify(projectService).createProject(any(Project.class));
    }

    @Test
    void projectEditSaveUpdatesProject() throws Exception {
        when(projectService.updateProject(eq(4L), any(Project.class))).thenReturn(sampleProject(4L));

        mockMvc.perform(post("/projectEdit")
                        .param("projectId", "4")
                        .param("name", "Updated"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projectList"));

        verify(projectService).updateProject(eq(4L), any(Project.class));
    }

    @Test
    void projectEditCancelRedirects() throws Exception {
        mockMvc.perform(post("/projectEdit").param("cancel", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projectList"));
    }

    @Test
    void projectEditDeleteRedirects() throws Exception {
        mockMvc.perform(post("/projectEdit")
                        .param("delete", "true")
                        .param("projectId", "6"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projectList"));

        verify(projectService).deleteProject(6L);
    }

    @Test
    void projectUsersWithoutProjectIdRedirects() throws Exception {
        mockMvc.perform(get("/projectUsers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projectList"));
    }

    @Test
    void projectUsersRemoveRedirects() throws Exception {
        mockMvc.perform(post("/projectUsers")
                        .param("removeUser", "true")
                        .param("projectId", "8")
                        .param("userId", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projectUsers?projectId=8"));

        verify(projectService).removeUserFromProject(8L, 3L);
    }

    private Project sampleProject(Long id) {
        Project project = new Project();
        project.setProjectId(id);
        project.setName("Project " + id);
        return project;
    }

    private Task sampleTask(Long id) {
        Task task = new Task();
        task.setTaskId(id);
        task.setName("Task " + id);
        return task;
    }

    private User sampleUser(Long id) {
        User user = new User();
        user.setUser_id(id);
        user.setUsername("user" + id);
        return user;
    }

    private FileInfo sampleFile(Long id) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setId(id);
        fileInfo.setName("file" + id + ".txt");
        return fileInfo;
    }
}
