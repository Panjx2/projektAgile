package com.example.app.controller;

import com.example.app.dto.MessageDTO;
import com.example.app.service.MessageService;
import com.example.app.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    public MessageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @GetMapping("/private")
    public List<MessageDTO> privateHistory(@RequestParam("withUserId") Long withUserId,
                                           Principal principal) {
        Long myId = userService.getByUsername(principal.getName()).getId();
        return messageService.getPrivateConversation(myId, withUserId);
    }

    @GetMapping("/project/{projectId}")
    public List<MessageDTO> projectHistory(@PathVariable Long projectId) {
        return messageService.getProjectMessages(projectId);
    }
}
