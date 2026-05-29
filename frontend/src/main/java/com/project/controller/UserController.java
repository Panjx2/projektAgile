package com.project.controller;

import com.project.exception.HttpException;
import com.project.model.User;
import com.project.service.UserService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Controller
public class UserController {

    private final UserService userService;
    private final RestClient restClient;

    public UserController(UserService userService, RestClient restClient) {
        this.userService = userService;
        this.restClient = restClient;
    }

    @GetMapping("/userList")
    public String userList(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "userList";
    }

    @GetMapping("/userEdit")
    public String userEdit(@RequestParam(required = false) Long userId, Model model) {
        if (userId != null) {
            model.addAttribute("user", userService.getUserById(userId));
        } else {
            model.addAttribute("user", new User());
        }
        return "userEdit";
    }

    @PostMapping("/userEdit")
    public String userEditSave(@ModelAttribute User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "userEdit";
        }
        try {
            if (user.getUser_id() == null) {
                userService.createUser(user);
            } else {
                userService.updateUser(user.getUser_id(), user);
            }
        } catch (HttpException e) {
            bindingResult.rejectValue(Strings.EMPTY, "http.error", e.getMessage());
            return "userEdit";
        }
        return "redirect:/userList";
    }

    @PostMapping(params = "cancel", path = "/userEdit")
    public String userEditCancel() {
        return "redirect:/userList";
    }

    @PostMapping(params = "delete", path = "/userEdit")
    public String userEditDelete(@ModelAttribute User user) {
        userService.deleteUser(user.getUser_id());
        return "redirect:/userList";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerSave(@ModelAttribute User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            restClient.post()
                    .uri("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(user)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            bindingResult.rejectValue(Strings.EMPTY, "http.error", "Błąd rejestracji");
            return "register";
        }
        return "redirect:/login";
    }

}
