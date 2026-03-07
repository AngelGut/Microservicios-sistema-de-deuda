package com.debtmanager.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración del PasswordEncoder.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Bean que se utilizará para encriptar y validar contraseñas.
     *
     * @return implementación BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
