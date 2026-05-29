package com.project.config;

import com.project.service.TokenStorage;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestClientInterceptor implements ClientHttpRequestInterceptor {

    private final TokenStorage tokenStorage;

    public RestClientInterceptor(TokenStorage tokenStorage) {
        this.tokenStorage = tokenStorage;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        if (tokenStorage.hasToken()) {
            request.getHeaders().setBearerAuth(tokenStorage.getToken());
        }

        return execution.execute(request, body);
    }
}