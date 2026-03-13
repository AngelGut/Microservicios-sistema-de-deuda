package com.example.notificationservice.service;

import com.example.notificationservice.dto.request.PaymentConfirmationRequest;
import com.example.notificationservice.dto.response.NotificationResponse;

/**
 * Contrato del servicio de notificaciones.
 *
 * <p>Orquesta la lógica de alto nivel: valida la solicitud,
 * delega el envío al {@link EmailService} y persiste el resultado
 * en {@code NotificationLog}.
 *
 * <p>Principio SRP: su única responsabilidad es coordinar
 * el flujo de notificación sin conocer los detalles de SMTP
 * ni de acceso a datos.
 *
 * <p>Principio OCP: nuevos tipos de notificación pueden agregarse
 * implementando esta interfaz o añadiendo métodos sin romper
 * los clientes existentes.
 */
public interface NotificationService {

    /**
     * Procesa y envía una notificación de confirmación de pago.
     *
     * @param request datos del pago confirmado por {@code payment-service}
     * @return respuesta con el resultado del envío y el ID del log generado
     */
    NotificationResponse processPaymentConfirmation(PaymentConfirmationRequest request);
}
