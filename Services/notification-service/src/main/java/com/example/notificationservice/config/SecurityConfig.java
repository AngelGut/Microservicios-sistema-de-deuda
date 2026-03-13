package com.example.notificationservice.config;

import com.example.notificationservice.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad HTTP del microservicio.
 *
 * <p>Sigue el mismo patrón que {@code payment-service}:
 * sesión stateless, JWT obligatorio en todos los endpoints
 * de negocio, y acceso libre a Swagger/Actuator.
 *
 * <p>Principio SRP: centraliza la configuración de seguridad
 * sin mezclarla con lógica de negocio.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // Swagger, OpenAPI y Actuator son públicos
                    .requestMatchers(
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/api-docs/**",
                            "/actuator/**"
                    ).permitAll()
                    // Todos los demás endpoints requieren JWT válido
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
