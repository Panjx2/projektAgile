package com.project.service;

import com.project.exception.HttpException;
import com.project.model.MessageDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class MessageService {

    private final RestClient restClient;

    public MessageService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<MessageDTO> getPrivateHistory(Long withUserId) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/messages/private")
                        .queryParam("withUserId", withUserId).build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new HttpException(res.getStatusCode(), res.getHeaders());
                })
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<MessageDTO> getProjectHistory(Long projectId) {
        return restClient.get()
                .uri("/api/messages/project/" + projectId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new HttpException(res.getStatusCode(), res.getHeaders());
                })
                .body(new ParameterizedTypeReference<>() {});
    }
}
