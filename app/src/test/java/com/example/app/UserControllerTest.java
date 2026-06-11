package com.example.app;

import com.example.app.controller.UserController;
import com.example.app.data.User;
import com.example.app.dto.UserDto;
import com.example.app.mapper.DtoMapper;
import com.example.app.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@WithMockUser(roles = "USER")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private DtoMapper dtoMapper;

    private UserDto buildDto(Long id, String username, String email) {
        UserDto dto = new UserDto();
        dto.setUserId(id);
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setFirstName("Jan");
        dto.setLastName("Kowalski");
        dto.setRole("ROLE_USER");
        return dto;
    }

    @Test
    void shouldCreateUser() throws Exception {
        User entity = new User();
        UserDto result = buildDto(1L, "jankowalski", "jan@test.com");

        when(dtoMapper.toEntity(any(UserDto.class))).thenReturn(entity);
        when(userService.createUser(entity)).thenReturn(entity);
        when(dtoMapper.toDto(entity)).thenReturn(result);

        String body = """
                {"username":"jankowalski","email":"jan@test.com","firstName":"Jan","lastName":"Kowalski","role":"ROLE_USER","password":"haslo123"}
                """;

        mockMvc.perform(post("/api/users").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(1))
                .andExpect(jsonPath("$.username").value("jankowalski"))
                .andExpect(jsonPath("$.email").value("jan@test.com"));
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        User u1 = new User();
        User u2 = new User();
        User u3 = new User();

        UserDto dto1 = buildDto(1L, "user1", "u1@test.com");
        UserDto dto2 = buildDto(2L, "user2", "u2@test.com");
        UserDto dto3 = buildDto(3L, "user3", "u3@test.com");

        when(userService.getAllUsers()).thenReturn(List.of(u1, u2, u3));
        when(dtoMapper.toDto(u1)).thenReturn(dto1);
        when(dtoMapper.toDto(u2)).thenReturn(dto2);
        when(dtoMapper.toDto(u3)).thenReturn(dto3);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[2].username").value("user3"));
    }

    @Test
    void shouldReturnEmptyListWhenNoUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldGetUserById() throws Exception {
        User entity = new User();
        UserDto dto = buildDto(3L, "testuser", "test@example.com");

        when(userService.getUserById(3L)).thenReturn(entity);
        when(dtoMapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(get("/api/users/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(3))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        when(userService.getUserById(404L)).thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/404"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        User entity = new User();
        UserDto result = buildDto(1L, "zmienionyuser", "zmieniony@test.com");

        when(dtoMapper.toEntity(any(UserDto.class))).thenReturn(entity);
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(entity);
        when(dtoMapper.toDto(entity)).thenReturn(result);

        String body = """
                {"username":"zmienionyuser","email":"zmieniony@test.com","firstName":"Jan","lastName":"Kowalski"}
                """;

        mockMvc.perform(put("/api/users/1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("zmienionyuser"))
                .andExpect(jsonPath("$.email").value("zmieniony@test.com"));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser(5L);

        mockMvc.perform(delete("/api/users/5").with(csrf()))
                .andExpect(status().isOk());

        verify(userService).deleteUser(5L);
    }

    @Test
    void shouldNotExposePasswordInResponse() throws Exception {
        User entity = new User();
        UserDto dto = buildDto(1L, "secureuser", "secure@test.com");

        when(userService.getUserById(1L)).thenReturn(entity);
        when(dtoMapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldReturnUserWithRole() throws Exception {
        User entity = new User();
        UserDto dto = buildDto(1L, "admin", "admin@test.com");
        dto.setRole("ROLE_ADMIN");

        when(userService.getUserById(1L)).thenReturn(entity);
        when(dtoMapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentUser() throws Exception {
        when(dtoMapper.toEntity(any(UserDto.class))).thenReturn(new User());
        when(userService.updateUser(eq(999L), any())).thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(put("/api/users/999").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }
}
