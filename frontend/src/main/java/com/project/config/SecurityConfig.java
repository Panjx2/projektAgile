package com.project.config;

import com.project.service.TokenStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SecurityConfig {
    @Value("${rest.base.url}")
    private String restBaseUrl;

    private final RestClientInterceptor restClientInterceptor;

    public SecurityConfig(RestClientInterceptor restClientInterceptor) {
        this.restClientInterceptor = restClientInterceptor;
    }

    @Bean
    public RestClient customRestClient() {
        return RestClient.builder()
                .baseUrl(restBaseUrl)
                .requestInterceptor(restClientInterceptor)
                .build();
    }
}