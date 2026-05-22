package com.project.controller;

import com.project.frontend.FrontendApplication;
import com.project.model.FileInfo;
import com.project.service.FileService;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
@ContextConfiguration(classes = FrontendApplication.class)
class FileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;

    @Test
    void uploadToProjectRedirects() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.txt",
                "text/plain",
                "data".getBytes(StandardCharsets.UTF_8)
        );
        when(fileService.uploadToProject(eq(1L), any(MultipartFile.class))).thenReturn(new FileInfo());

        mockMvc.perform(multipart("/files/project/{projectId}", 1L).file(file))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projectDetails?projectId=1"));
    }

    @Test
    void uploadToTaskRedirects() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "task.txt",
                "text/plain",
                "data".getBytes(StandardCharsets.UTF_8)
        );
        when(fileService.uploadToTask(eq(2L), any(MultipartFile.class))).thenReturn(new FileInfo());

        mockMvc.perform(multipart("/files/task/{taskId}", 2L)
                        .file(file)
                        .param("projectId", "9"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/taskEdit?taskId=2&projectId=9"));
    }

    @Test
    void deleteFileRedirectsToProjectDetails() throws Exception {
        mockMvc.perform(post("/files/{fileId}/delete", 4L)
                        .param("projectId", "7"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projectDetails?projectId=7"));

        verify(fileService).deleteFile(4L);
    }

    @Test
    void downloadFileReturnsBytes() throws Exception {
        byte[] payload = "download".getBytes(StandardCharsets.UTF_8);
        when(fileService.downloadFile(6L)).thenReturn(ResponseEntity.ok(payload));

        mockMvc.perform(get("/files/{fileId}/download", 6L))
                .andExpect(status().isOk())
                .andExpect(content().bytes(payload));
    }
}
