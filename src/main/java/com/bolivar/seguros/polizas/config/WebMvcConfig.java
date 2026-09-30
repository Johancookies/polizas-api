package com.bolivar.seguros.polizas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.bolivar.seguros.polizas.model.PolicyStatus;
import com.bolivar.seguros.polizas.model.PolicyType;
import org.springframework.format.FormatterRegistry;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApiKeyInterceptor apiKeyInterceptor;

    public WebMvcConfig(ApiKeyInterceptor apiKeyInterceptor) {
        this.apiKeyInterceptor = apiKeyInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/v3/api-docs",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/h2-console/**",
                        "/core-mock/**",
                        "/favicon.ico",
                        "/error"
                );
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, PolicyType.class, source -> {
            if (source == null || source.isBlank()) return null;
            try {
                return PolicyType.valueOf(source.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Tipo de póliza inválido: '" + source + "'. Valores permitidos: INDIVIDUAL, COLECTIVA");
            }
        });

        registry.addConverter(String.class, PolicyStatus.class, source -> {
            if (source == null || source.isBlank()) return null;
            try {
                return PolicyStatus.valueOf(source.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Estado de póliza inválido: '" + source + "'. Valores permitidos: ACTIVA, RENOVADA, CANCELADA");
            }
        });
    }
}
