package com.example.userservice.config;

import com.example.userservice.entity.Role;
import com.example.userservice.entity.User;
import com.example.userservice.repository.RoleRepository;
import com.example.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Configuration
public class AdminUserInitializer {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    @Bean
    public CommandLineRunner seedAdminUser(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed-admin.enabled:true}") boolean seedEnabled,
            @Value("${app.seed-admin.email:admin@deuda.local}") String adminEmail,
            @Value("${app.seed-admin.username:admin}") String adminUsername,
            @Value("${app.seed-admin.full-name:Administrador}") String adminFullName,
            @Value("${app.seed-admin.password:Admin$123}") String adminPassword) {

        return args -> {
            if (!seedEnabled) {
                log.info("Seed de admin deshabilitado por configuración.");
                return;
            }

            if (userRepository.existsByEmail(adminEmail)) {
                log.info("Usuario admin ya existe: {}", adminEmail);
                return;
            }

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .id("1")
                            .name("ADMIN")
                            .description("Administrador del sistema")
                            .createdAt(LocalDateTime.now())
                            .build()));

            User admin = User.builder()
                    .id(UUID.randomUUID().toString())
                    .username(adminUsername)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .fullName(adminFullName)
                    .active(true)
                    .roles(Set.of(adminRole))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            userRepository.save(admin);
            log.info("Usuario admin creado automáticamente: {}", adminEmail);
        };
    }
}
