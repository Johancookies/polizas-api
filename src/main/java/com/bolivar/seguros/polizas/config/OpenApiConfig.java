package com.bolivar.seguros.polizas.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "ApiKeyAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Seguros Bolívar - API de Gestión de Pólizas")
                        .version("1.0.0")
                        .description("API RESTful para la gestión del ciclo de vida de pólizas de arrendamiento (Individuales y Colectivas) y sus riesgos asociados, con integración asíncrona hacia el CORE transaccional legado.")
                        .contact(new Contact().name("Johan Sebastian Mancilla Rincon").email("johanmancillari@gmail.com")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name("x.api-key")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Header de autenticación obligatorio. Valor por defecto de prueba: 123456 (también acepta 'api-key: 123456')")));
    }
}
