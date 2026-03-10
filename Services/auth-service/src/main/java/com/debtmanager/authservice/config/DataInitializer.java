package com.debtmanager.authservice.config;

import com.debtmanager.authservice.domain.model.User;
import com.debtmanager.authservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Inicialización de datos al arranque del auth-service.
 * Crea el usuario administrador si no existe todavía.
 */
@Configuration
public class DataInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner seedAdminUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed-admin.email:admin@tejada.com}") String adminEmail,
            @Value("${app.seed-admin.password:Admin2026!}") String adminPassword) {

        return args -> {
            if (userRepository.findByEmail(adminEmail).isPresent()) {
                LOGGER.info("Usuario admin ya existe: {}", adminEmail);
                return;
            }

            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole("ADMIN");
            admin.setEnabled(true);

            userRepository.save(admin);
            LOGGER.info("✅ Usuario admin creado exitosamente: {}", adminEmail);
        };
    }
}
