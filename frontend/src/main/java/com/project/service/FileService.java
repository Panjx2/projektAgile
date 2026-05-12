package com.project.service;

import com.project.model.FileInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {
    List<FileInfo> getFilesByProject(Long projectId);
    List<FileInfo> getFilesByTask(Long taskId);
    FileInfo uploadToProject(Long projectId, MultipartFile file) throws IOException;
    FileInfo uploadToTask(Long taskId, MultipartFile file) throws IOException;
    void deleteFile(Long fileId);
    ResponseEntity<byte[]> downloadFile(Long fileId);
}
