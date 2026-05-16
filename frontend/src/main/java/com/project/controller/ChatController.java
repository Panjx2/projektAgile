package com.project.controller;

import com.project.model.MessageDTO;
import com.project.model.Project;
import com.project.model.User;
import com.project.service.MessageService;
import com.project.service.ProjectService;
import com.project.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ChatController {

    private final UserService userService;
    private final ProjectService projectService;
    private final MessageService messageService;

    @Value("${ws.public.url}")
    private String wsPublicUrl;

    public ChatController(UserService userService,
                          ProjectService projectService,
                          MessageService messageService) {
        this.userService = userService;
        this.projectService = projectService;
        this.messageService = messageService;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(required = false) Long withUserId,
                       @RequestParam(required = false) Long projectId,
                       Model model) {

        User me = userService.getCurrentUser();
        List<User> users = userService.getAllUsers().stream()
                .filter(u -> !u.getUser_id().equals(me.getUser_id()))
                .toList();
        List<Project> projects = projectService.getAllProjects();

        model.addAttribute("me", me);
        model.addAttribute("users", users);
        model.addAttribute("projects", projects);
        model.addAttribute("wsPublicUrl", wsPublicUrl);

        if (withUserId != null) {
            User other = userService.getUserById(withUserId);
            List<MessageDTO> messages = messageService.getPrivateHistory(withUserId);
            model.addAttribute("conversationType", "private");
            model.addAttribute("conversationWith", other);
            model.addAttribute("messages", messages);
        } else if (projectId != null) {
            Project project = projectService.getProjectById(projectId);
            List<MessageDTO> messages = messageService.getProjectHistory(projectId);
            model.addAttribute("conversationType", "project");
            model.addAttribute("conversationProject", project);
            model.addAttribute("messages", messages);
        } else {
            model.addAttribute("conversationType", "none");
            model.addAttribute("messages", List.of());
        }

        return "chat";
    }
}
