package com.debtmanager.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal del microservicio auth-service.
 *
 * Spring Boot inicia desde aquí y escanea todos los paquetes hijos de
 * com.debtmanager.authservice.
 */
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
