package com.project.controller;

import com.project.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/project/{projectId}")
    public String uploadToProject(@PathVariable Long projectId,
                                  @RequestParam("file") MultipartFile file) throws IOException {
        fileService.uploadToProject(projectId, file);
        return "redirect:/projectDetails?projectId=" + projectId;
    }

    @PostMapping("/task/{taskId}")
    public String uploadToTask(@PathVariable Long taskId,
                               @RequestParam("file") MultipartFile file,
                               @RequestParam Long projectId) throws IOException {
        fileService.uploadToTask(taskId, file);
        return "redirect:/taskEdit?taskId=" + taskId + "&projectId=" + projectId;
    }

    @PostMapping("/{fileId}/delete")
    public String deleteFile(@PathVariable Long fileId,
                             @RequestParam(required = false) Long projectId,
                             @RequestParam(required = false) Long taskId,
                             @RequestParam(required = false) Long taskProjectId) {
        fileService.deleteFile(fileId);
        if (projectId != null) {
            return "redirect:/projectDetails?projectId=" + projectId;
        }
        return "redirect:/taskEdit?taskId=" + taskId + "&projectId=" + taskProjectId;
    }

    @GetMapping("/{fileId}/download")
    @ResponseBody
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) {
        return fileService.downloadFile(fileId);
    }
}
