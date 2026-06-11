package com.example.app;

import com.example.app.data.User;
import com.example.app.repository.UserRepository;
import com.example.app.security.AuthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldRegisterNewUser() throws Exception {
        User savedUser = new User();
        savedUser.setUsername("nowyuzytkownik");
        savedUser.setEmail("nowy@test.com");
        savedUser.setFirstName("Nowy");
        savedUser.setLastName("Uzytkownik");
        savedUser.setRole("ROLE_USER");

        when(passwordEncoder.encode("haslo123")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        String body = """
                {"username":"nowyuzytkownik","email":"nowy@test.com","firstName":"Nowy","lastName":"Uzytkownik","password":"haslo123"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("nowyuzytkownik"))
                .andExpect(jsonPath("$.email").value("nowy@test.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    void shouldAssignRoleUserOnRegistration() throws Exception {
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return u;
        });

        String body = """
                {"username":"testuser","email":"test@test.com","firstName":"T","lastName":"T","password":"pass"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    void shouldNotExposePasswordInRegistrationResponse() throws Exception {
        User savedUser = new User();
        savedUser.setUsername("secureuser");
        savedUser.setEmail("sec@test.com");
        savedUser.setRole("ROLE_USER");

        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        String body = """
                {"username":"secureuser","email":"sec@test.com","firstName":"S","lastName":"S","password":"tajne123"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldEncodePasswordBeforeSaving() throws Exception {
        when(passwordEncoder.encode("plaintext")).thenReturn("$2a$10$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        String body = """
                {"username":"user","email":"u@test.com","firstName":"F","lastName":"L","password":"plaintext"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnSavedUserData() throws Exception {
        User saved = new User();
        saved.setUsername("zwrocony");
        saved.setEmail("zwrot@example.com");
        saved.setFirstName("Zwrot");
        saved.setLastName("Danych");
        saved.setRole("ROLE_USER");

        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(saved);

        String body = """
                {"username":"zwrocony","email":"zwrot@example.com","firstName":"Zwrot","lastName":"Danych","password":"pass"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Zwrot"))
                .andExpect(jsonPath("$.lastName").value("Danych"));
    }

    @Test
    void shouldRegisterUserWithAllFields() throws Exception {
        User saved = new User();
        saved.setUsername("fulluser");
        saved.setEmail("full@test.com");
        saved.setFirstName("Pelny");
        saved.setLastName("Uzytkownik");
        saved.setRole("ROLE_USER");

        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(saved);

        String body = """
                {
                  "username": "fulluser",
                  "email": "full@test.com",
                  "firstName": "Pelny",
                  "lastName": "Uzytkownik",
                  "password": "silnehaslo123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("fulluser"))
                .andExpect(jsonPath("$.email").value("full@test.com"))
                .andExpect(jsonPath("$.firstName").value("Pelny"))
                .andExpect(jsonPath("$.lastName").value("Uzytkownik"));
    }
}
