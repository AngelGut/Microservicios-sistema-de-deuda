package com.example.debtservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de seguridad del debt-service.
 * Registra el JwtFilter para que intercepte todos los requests.
 * No usa Spring Security — validación JWT manual.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    /** Filtro JWT que valida el token en cada request */
    private final JwtFilter jwtFilter;

    /**
     * Registra el JwtFilter en el contenedor de Spring.
     * Se aplica a todos los endpoints del servicio.
     * Order 1 = se ejecuta primero antes que otros filtros.
     */
    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration() {

        FilterRegistrationBean<JwtFilter> registration = new FilterRegistrationBean<>();

        // Registramos nuestro filtro JWT
        registration.setFilter(jwtFilter);

        // Se aplica a todos los endpoints
        registration.addUrlPatterns("/debts/*", "/debts");

        // Prioridad alta — se ejecuta primero
        registration.setOrder(1);

        return registration;
    }
}
