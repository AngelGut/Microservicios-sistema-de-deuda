package com.example.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CorsConfig
 * ----------
 * Configuración centralizada de CORS para el API Gateway.
 *
 * Permite que el web-ui (Thymeleaf en :8090) pueda llamar al gateway
 * sin errores de Cross-Origin en el navegador.
 *
 * Principio SRP: esta clase solo maneja CORS, nada más.
 */
@Configuration
public class CorsConfig {

    /**
     * Filtro reactivo de CORS para WebFlux.
     * (No usar CorsFilter de Servlet — el gateway es reactivo)
     */
    @Bean
    public CorsWebFilter corsWebFilter() {

        CorsConfiguration config = new CorsConfiguration();

        // Orígenes permitidos — web-ui local y Railway
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:8090",       // web-ui en desarrollo local
                "https://*.railway.app"        // cualquier servicio en Railway
        ));

        // Métodos HTTP permitidos
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Headers permitidos — incluye Authorization y X-Trace-Id del equipo
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Trace-Id",
                "Accept",
                "Origin"
        ));

        // Headers que el cliente puede leer en el response
        config.setExposedHeaders(List.of("X-Trace-Id", "Authorization"));

        // Permite cookies y credenciales (necesario para JWT en cookie HttpOnly)
        config.setAllowCredentials(true);

        // Tiempo de caché del preflight en segundos
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
