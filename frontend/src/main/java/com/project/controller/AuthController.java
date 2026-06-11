package com.project.controller;

import com.project.service.TokenStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final RestClient restClient;
    private final TokenStorage tokenStorage;

    public AuthController(RestClient restClient, TokenStorage tokenStorage) {
        this.restClient = restClient;
        this.tokenStorage = tokenStorage;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        Model model) {
        try {
            ResponseEntity<Map> response = restClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("username", username, "password", password))
                    .retrieve()
                    .toEntity(Map.class);

            Map<String, String> body = response.getBody();
            if (body != null) {
                tokenStorage.setToken(body.get("token"));
                tokenStorage.setUsername(body.get("username"));
                tokenStorage.setRole(body.get("role"));
                logger.info("User {} logged in successfully", username);
                return "redirect:/projectList";
            }
        } catch (Exception e) {
            logger.error("Login failed for user {}", username, e);
            model.addAttribute("error", "Niepoprawny login lub hasło");
            return "login";
        }

        model.addAttribute("error", "Niepoprawny login lub hasło");
        return "login";
    }

    @GetMapping("/logout")
    public String logout() {
        tokenStorage.clear();
        return "redirect:/login";
    }
}