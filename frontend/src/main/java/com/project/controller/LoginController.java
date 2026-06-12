package com.project.controller;

import com.project.service.TokenStorage;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class LoginController {

    private final TokenStorage tokenStorage;

    public LoginController(TokenStorage tokenStorage) {
        this.tokenStorage = tokenStorage;
    }

    @ModelAttribute("loggedIn")
    public Boolean loggedIn() {
        return tokenStorage.hasToken();
    }

    @ModelAttribute("username")
    public String username() {
        return tokenStorage.getUsername();
    }

    @ModelAttribute("isAdmin")
    public Boolean isAdmin() {
        return "ROLE_ADMIN".equals(tokenStorage.getRole());
    }
}
