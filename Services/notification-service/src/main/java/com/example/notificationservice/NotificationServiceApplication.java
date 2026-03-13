package com.example.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada del microservicio de notificaciones.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Enviar correos de confirmación de pago (endpoint REST consumido por payment-service).</li>
 *   <li>Enviar recordatorios de vencimiento de deuda (tarea programada diaria).</li>
 * </ul>
 */
@SpringBootApplication
@EnableScheduling
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
