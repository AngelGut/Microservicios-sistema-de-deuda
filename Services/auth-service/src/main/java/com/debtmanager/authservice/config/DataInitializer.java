package com.debtmanager.authservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Inicialización liviana de arranque.
 *
 * auth-service ya no persiste usuarios locales para login;
 * delega validación de credenciales al user-service.
 */
@Configuration
public class DataInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner startupInfo() {
        return args -> LOGGER.info("Auth-service iniciado en modo delegación de credenciales a user-service");
    }
}
