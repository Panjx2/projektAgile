package com.project.service;

import com.project.exception.HttpException;
import com.project.model.FileInfo;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class FileServiceImpl implements FileService {

    private final RestClient restClient;

    public FileServiceImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<FileInfo> getFilesByProject(Long projectId) {
        return restClient.get()
                .uri("/api/files/project/" + projectId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new HttpException(res.getStatusCode(), res.getHeaders());
                })
                .body(new ParameterizedTypeReference<>() {});
    }

    @Override
    public List<FileInfo> getFilesByTask(Long taskId) {
        return restClient.get()
                .uri("/api/files/task/" + taskId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new HttpException(res.getStatusCode(), res.getHeaders());
                })
                .body(new ParameterizedTypeReference<>() {});
    }

    @Override
    public FileInfo uploadToProject(Long projectId, MultipartFile file) throws IOException {
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new InputStreamResource(file.getInputStream()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }

            @Override
            public long contentLength() {
                return file.getSize();
            }
        });

        return restClient.post()
                .uri("/api/files/project/" + projectId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new HttpException(res.getStatusCode(), res.getHeaders());
                })
                .body(FileInfo.class);
    }

    @Override
    public FileInfo uploadToTask(Long taskId, MultipartFile file) throws IOException {
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new InputStreamResource(file.getInputStream()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }

            @Override
            public long contentLength() {
                return file.getSize();
            }
        });

        return restClient.post()
                .uri("/api/files/task/" + taskId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new HttpException(res.getStatusCode(), res.getHeaders());
                })
                .body(FileInfo.class);
    }

    @Override
    public void deleteFile(Long fileId) {
        restClient.delete()
                .uri("/api/files/" + fileId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new HttpException(res.getStatusCode(), res.getHeaders());
                })
                .toBodilessEntity();
    }

    @Override
    public ResponseEntity<byte[]> downloadFile(Long fileId) {
        return restClient.get()
                .uri("/api/files/" + fileId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new HttpException(res.getStatusCode(), res.getHeaders());
                })
                .toEntity(byte[].class);
    }
}
