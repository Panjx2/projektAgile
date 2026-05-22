package com.project.controller;

import com.project.frontend.FrontendApplication;
import com.project.model.User;
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

@WebMvcTest(UserController.class)
@ContextConfiguration(classes = FrontendApplication.class)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void userListRendersView() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(sampleUser(1L)));

        mockMvc.perform(get("/userList"))
                .andExpect(status().isOk())
                .andExpect(view().name("userList"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    void userEditWithoutIdCreatesNewUser() throws Exception {
        mockMvc.perform(get("/userEdit"))
                .andExpect(status().isOk())
                .andExpect(view().name("userEdit"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void userEditWithIdLoadsUser() throws Exception {
        when(userService.getUserById(2L)).thenReturn(sampleUser(2L));

        mockMvc.perform(get("/userEdit").param("userId", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("userEdit"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void userEditSaveCreatesUser() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(sampleUser(3L));

        mockMvc.perform(post("/userEdit")
                        .param("username", "john")
                        .param("email", "john@example.com")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("role", "USER")
                        .param("password", "secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/userList"));

        verify(userService).createUser(any(User.class));
    }

    @Test
    void userEditSaveUpdatesUser() throws Exception {
        when(userService.updateUser(eq(5L), any(User.class))).thenReturn(sampleUser(5L));

        mockMvc.perform(post("/userEdit")
                        .param("user_id", "5")
                        .param("username", "john")
                        .param("email", "john@example.com")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("role", "ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/userList"));

        verify(userService).updateUser(eq(5L), any(User.class));
    }

    @Test
    void userEditCancelRedirects() throws Exception {
        mockMvc.perform(post("/userEdit").param("cancel", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/userList"));
    }

    @Test
    void userEditDeleteRedirects() throws Exception {
        mockMvc.perform(post("/userEdit")
                        .param("delete", "true")
                        .param("user_id", "6"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/userList"));

        verify(userService).deleteUser(6L);
    }

    private User sampleUser(Long id) {
        User user = new User();
        user.setUser_id(id);
        user.setUsername("user" + id);
        user.setEmail("user" + id + "@example.com");
        return user;
    }
}
