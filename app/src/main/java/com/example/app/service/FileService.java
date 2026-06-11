package com.example.app.service;

import com.example.app.data.FileEntity;
import com.example.app.data.Project;
import com.example.app.data.Task;
import com.example.app.repository.FileEntityRepository;
import com.example.app.repository.ProjectRepository;
import com.example.app.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final FileEntityRepository fileRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public FileService(FileEntityRepository fileRepository,
                       ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.fileRepository = fileRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    public FileEntity uploadToProject(Long projectId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File too large. Max size is 10MB.");
        }
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        String originalName = sanitizeFilename(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "_" + originalName;
        Path path = Paths.get(uploadDir, fileName);

        Files.createDirectories(path.getParent());
        file.transferTo(path);

        FileEntity entity = new FileEntity();
        entity.setName(originalName);
        entity.setPath(path.toString());
        entity.setProject(project);

        return fileRepository.save(entity);
    }

    public FileEntity uploadToTask(Long taskId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File too large. Max size is 10MB.");
        }
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        String originalName = sanitizeFilename(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "_" + originalName;
        Path path = Paths.get(uploadDir, fileName);

        Files.createDirectories(path.getParent());
        file.transferTo(path);

        FileEntity entity = new FileEntity();
        entity.setName(originalName);
        entity.setPath(path.toString());
        entity.setTask(task);
        entity.setProject(null);

        return fileRepository.save(entity);
    }

    public List<FileEntity> getFilesByProject(Long projectId) {

        if (!projectRepository.existsById(projectId)) {
            throw new EntityNotFoundException("Project not found");
        }
        return fileRepository.findByProjectId(projectId);
    }

    public List<FileEntity> getFilesByTask(Long taskId) {

        if (!taskRepository.existsById(taskId)) {
            throw new EntityNotFoundException("Task not found");
        }
        return fileRepository.findByTaskId(taskId);
    }


    public FileEntity getFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("File not found: " + fileId));
    }

    public Resource downloadFile(Long fileId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("File not found: " + fileId));

        Path path = Paths.get(file.getPath());
        if (!Files.exists(path)) {
            throw new EntityNotFoundException("File not found on disk");
        }
        return new FileSystemResource(path);
    }

    public void deleteFile(Long fileId) throws IOException {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow();

        Path path = Paths.get(file.getPath());
        Files.deleteIfExists(path);
        fileRepository.delete(file);
    }

    private String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "unnamed_file";
        }
        // strip any directory traversal — keep only the last path component
        return Paths.get(originalFilename).getFileName().toString();
    }
}