package com.example.paymentservice.service.impl;

// ============================================================
//  EJEMPLO DE INTEGRACIÓN
//  Este archivo muestra cómo payment-service debe llamar a
//  notification-service después de registrar un pago.
//
//  NO es parte del notification-service.
//  Agregar este código en PaymentServiceImpl del payment-service.
// ============================================================

import com.example.paymentservice.dto.request.CreatePaymentRequest;
import com.example.paymentservice.dto.response.DebtResponse;
import com.example.paymentservice.dto.response.PaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Map;

/**
 * Fragmento de código de integración para PaymentServiceImpl.
 *
 * <p>Después del paso 5 (debtClient.notifyPayment), agregar:
 *
 * <pre>
 *   // 6. Notificar al notification-service para envío de confirmación por email
 *   notificationClient.sendPaymentConfirmation(saved, debt, request);
 * </pre>
 *
 * <p>Agregar el bean NotificationClient al constructor e inyectarlo
 * como dependencia (principio DIP).
 */
public class NotificationClientExample {

    private static final Logger log = LoggerFactory.getLogger(NotificationClientExample.class);

    private final RestTemplate restTemplate;
    private final String notificationServiceUrl;

    public NotificationClientExample(
            RestTemplate restTemplate,
            @Value("${app.notification-service.base-url:http://localhost:8085}") String notificationServiceUrl) {
        this.restTemplate          = restTemplate;
        this.notificationServiceUrl = notificationServiceUrl;
    }

    /**
     * Envía la notificación de confirmación de pago a notification-service.
     *
     * <p>La llamada es fire-and-forget con manejo de errores:
     * si notification-service falla, el pago ya está registrado y
     * solo se registra un warning en el log. No se debe lanzar
     * excepción al cliente por un fallo de notificación.
     *
     * @param payment respuesta del pago recién registrado
     * @param debt    datos de la deuda del payment-service
     * @param debtorName  nombre del deudor (obtenido de debtor-service si es necesario)
     * @param debtorEmail email del deudor (obtenido de debtor-service)
     */
    public void sendPaymentConfirmation(
            PaymentResponse payment,
            DebtResponse debt,
            String debtorName,
            String debtorEmail) {

        try {
            // ── Construir el DTO de la request ───────────────
            Map<String, Object> body = Map.of(
                "debtId",           payment.debtId(),
                "debtorId",         debt.debtorId(),
                "debtorName",       debtorName,
                "debtorEmail",      debtorEmail,
                "amountPaid",       payment.amount(),
                "remainingBalance", debt.currentBalance(),
                "currency",         debt.currency(),
                "paymentDate",      payment.paymentDate().toString()
            );

            ResponseEntity<Void> response = restTemplate.postForEntity(
                    notificationServiceUrl + "/api/v1/notifications/payment-confirmation",
                    body,
                    Void.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Notificación de pago enviada a notification-service para deuda {}",
                        payment.debtId());
            }

        } catch (Exception ex) {
            // No propagar el error: el pago ya fue registrado exitosamente.
            // Solo loguear para diagnóstico.
            log.warn("No se pudo notificar a notification-service para deuda {}: {}",
                    payment.debtId(), ex.getMessage());
        }
    }
}
