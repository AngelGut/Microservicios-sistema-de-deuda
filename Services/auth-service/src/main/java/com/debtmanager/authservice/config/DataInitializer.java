package com.debtmanager.authservice.config;

import com.debtmanager.authservice.domain.model.User;
import com.debtmanager.authservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Inserta un usuario de prueba si aún no existe.
 *
 * Esto sirve para probar rápidamente el login del microservicio.
 */
@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@debtmanager.com";

            boolean exists = userRepository.findByEmail(adminEmail).isPresent();

            if (!exists) {
                User user = new User();
                user.setEmail(adminEmail);
                user.setPassword(passwordEncoder.encode("Admin123*"));
                user.setRole("ADMIN");
                user.setEnabled(true);

                userRepository.save(user);
            }
        };
    }
}
