package com.bolivar.seguros.polizas.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    private static final String API_KEY_HEADER = "api-key";
    private static final String API_KEY_VALUE = "123456";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (API_KEY_VALUE.equals(apiKey)) {
            return true;
        }
        
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write("No autorizado. api-key invalida o ausente.");
        return false;
    }
}
