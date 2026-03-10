package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del API Gateway.
 * Actúa como punto de entrada único para todos los microservicios del sistema.
 *
 * Servicios enrutados:
 *   - auth-service    :8081  → /api/v1/auth/**
 *   - debtor-service  :8082  → /api/v1/debtors/**
 *   - debt-service    :8083  → /api/v1/debts/**
 *   - payment-service :8084  → /api/v1/payments/**
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
