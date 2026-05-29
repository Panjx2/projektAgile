package com.project.controller;

import com.project.service.TokenStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
public class ValidationProxyController {

    private final RestClient restClient;
    private final TokenStorage tokenStorage;

    public ValidationProxyController(RestClient restClient, TokenStorage tokenStorage) {
        this.restClient = restClient;
        this.tokenStorage = tokenStorage;
    }

    @GetMapping("/api/users/validate-username")
    public ResponseEntity<Boolean> validateUsername(@RequestParam String username) {
        Boolean result = restClient.get()
                .uri("/api/users/validate-username?username={username}", username)
                .retrieve()
                .body(Boolean.class);
        return ResponseEntity.ok(result != null && result);
    }

    @GetMapping("/api/users/validate-email")
    public ResponseEntity<Boolean> validateEmail(@RequestParam String email) {
        Boolean result = restClient.get()
                .uri("/api/users/validate-email?email={email}", email)
                .retrieve()
                .body(Boolean.class);
        return ResponseEntity.ok(result != null && result);
    }
}