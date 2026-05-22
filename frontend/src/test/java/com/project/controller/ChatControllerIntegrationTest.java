package com.project.controller;

import com.project.frontend.FrontendApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ContextConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ChatController.class)
@ContextConfiguration(classes = FrontendApplication.class)
class ChatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void chatGetRendersView() throws Exception {
        mockMvc.perform(get("/chat"))
                .andExpect(status().isOk())
                .andExpect(view().name("chat"));
    }

    @Test
    void chatPostRedirects() throws Exception {
        mockMvc.perform(post("/chat"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/chat"));
    }
}
