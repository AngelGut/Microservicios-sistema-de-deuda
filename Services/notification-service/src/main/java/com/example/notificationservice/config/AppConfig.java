package com.example.notificationservice.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuración de beans de infraestructura para la aplicación.
 *
 * <p>Principio SRP: centraliza la declaración de beans que no
 * pertenecen a una capa específica (web, service, persistence).
 *
 * <p>Principio DIP: expone {@link RestTemplate} como bean inyectable,
 * permitiendo que los clientes HTTP dependan de él sin conocer
 * los detalles de su configuración.
 */
@Configuration
public class AppConfig {

    /**
     * {@link RestTemplate} preconfigurado con timeouts razonables
     * para la comunicación inter-servicios.
     *
     * <p>Los timeouts evitan que una llamada bloqueante a un servicio
     * caído afecte la disponibilidad de notification-service.
     *
     * @param builder builder provisto por Spring Boot con auto-configuración
     * @return instancia de RestTemplate lista para inyectar
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(5))
                .build();
    }
}
