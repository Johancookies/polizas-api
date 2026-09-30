package com.bolivar.seguros.polizas.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    private static final String API_KEY_VALUE = "123456";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String apiKey = request.getHeader("x.api-key");
        if (apiKey == null) {
            apiKey = request.getHeader("x-api-key");
        }
        if (apiKey == null) {
            apiKey = request.getHeader("api-key");
        }

        if (API_KEY_VALUE.equals(apiKey)) {
            return true;
        }
        
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\": \"No autorizado\", \"mensaje\": \"Header de autenticación inválido o ausente. Use 'x.api-key: 123456' o 'api-key: 123456'.\"}");
        return false;
    }
}
