package com.project.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class LoginController {
    @ModelAttribute("loggedIn")
    public Boolean loggedIn(Principal principal) {
        return principal != null;
    }

    @ModelAttribute("username")
    public String username(Principal principal) {
        return principal != null ? principal.getName() : null;
    }
}