package com.example.notificationservice.dto.response;

import java.time.LocalDateTime;

/**
 * DTO de respuesta devuelto al llamador después de procesar
 * una solicitud de notificación.
 *
 * <p>Principio SRP: separa la vista de respuesta del modelo
 * de dominio interno {@code NotificationLog}.
 */
public record NotificationResponse(

        /** ID del registro de notificación generado. */
        Long id,

        /** Tipo de notificación: PAYMENT_CONFIRMATION | PAYMENT_REMINDER. */
        String type,

        /** Dirección de correo del destinatario. */
        String recipientEmail,

        /** Estado del envío: SENT | FAILED. */
        String status,

        /** Mensaje descriptivo del resultado. */
        String message,

        /** Marca temporal del procesamiento. */
        LocalDateTime processedAt
) {}
