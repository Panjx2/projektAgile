package com.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ChatController {

    @GetMapping("/chat")
    public String chat() {
        return "chat";
    }

    @PostMapping("/chat")
    public String chatSend() {
        return "redirect:/chat";
    }
}