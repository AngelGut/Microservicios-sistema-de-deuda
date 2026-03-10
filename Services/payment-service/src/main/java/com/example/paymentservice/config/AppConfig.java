package com.example.paymentservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Beans de infraestructura de la aplicación.
 *
 * <p>Principio SRP: gestiona la creación de beans de infraestructura.
 */
@Configuration
public class AppConfig {

    /**
     * RestTemplate para comunicación HTTP hacia debt-service.
     * En producción se puede enriquecer con interceptores para pasar el traceId.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Configuración de OpenAPI / Swagger con soporte para JWT Bearer.
     */
    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Payment Service API")
                        .description("Microservicio de gestión de pagos – Sistema de Deudas")
                        .version("0.0.1-SNAPSHOT"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
