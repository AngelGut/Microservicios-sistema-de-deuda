package com.debtmanager.authservice.config;

import com.debtmanager.authservice.domain.model.User;
import com.debtmanager.authservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Garantiza la creación del usuario administrador por defecto al iniciar.
 */
@Configuration
public class DataInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);
    private static final String DEFAULT_ADMIN_EMAIL = "admin@debtmanager.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin123*";
    private static final String ADMIN_ROLE = "ADMIN";

    @Bean
    public CommandLineRunner initAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail(DEFAULT_ADMIN_EMAIL).isEmpty()) {
                User user = new User();
                user.setEmail(DEFAULT_ADMIN_EMAIL);
                user.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
                user.setRole(ADMIN_ROLE);
                user.setEnabled(true);

                userRepository.save(user);
                LOGGER.info("Default admin user created for email {}", DEFAULT_ADMIN_EMAIL);
            } else {
                LOGGER.debug("Default admin user already exists for email {}", DEFAULT_ADMIN_EMAIL);
            }
        };
    }
}
