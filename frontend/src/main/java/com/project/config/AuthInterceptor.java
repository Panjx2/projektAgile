package com.project.config;

import com.project.service.TokenStorage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenStorage tokenStorage;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/login",
            "/register",
            "/api/",
            "/css/",
            "/js/",
            "/images/",
            "/favicon.ico",
            "/error"
    );

    public AuthInterceptor(TokenStorage tokenStorage) {
        this.tokenStorage = tokenStorage;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String path = request.getRequestURI();

        // Allow public paths
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }

        // If user has a token, allow access
        if (tokenStorage.hasToken()) {
            return true;
        }

        // Otherwise redirect to login
        response.sendRedirect("/login");
        return false;
    }
}