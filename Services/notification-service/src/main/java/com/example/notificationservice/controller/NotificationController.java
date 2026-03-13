package com.example.notificationservice.controller;

import com.example.notificationservice.dto.request.PaymentConfirmationRequest;
import com.example.notificationservice.dto.response.NotificationResponse;
import com.example.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST del microservicio de notificaciones.
 *
 * <p>Expone los endpoints que los demás microservicios (principalmente
 * {@code payment-service}) utilizan para disparar el envío de correos.
 *
 * <p>Principio SRP: solo mapea peticiones HTTP a servicios de aplicación;
 * no contiene lógica de negocio.
 *
 * <p>Principio DIP: depende de la interfaz {@link NotificationService},
 * no de su implementación concreta.
 *
 * <p>Base URL: {@code /api/v1/notifications}
 */
@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Endpoints para el envío de notificaciones por email")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ── POST /payment-confirmation ───────────────────────────

    /**
     * Recibe el evento de pago confirmado desde {@code payment-service}
     * y envía un correo de confirmación al deudor.
     *
     * <p>Este endpoint es invocado de forma interna por {@code payment-service}
     * después de registrar exitosamente un pago. No debe exponerse
     * directamente al cliente externo (bloquear en api-gateway si se requiere).
     *
     * <p>Ejemplo de request:
     * <pre>
     * POST /api/v1/notifications/payment-confirmation
     * {
     *   "debtId":           "uuid-deuda-001",
     *   "debtorId":         "42",
     *   "debtorName":       "Juan Pérez",
     *   "debtorEmail":      "juan.perez@email.com",
     *   "amountPaid":       150.00,
     *   "remainingBalance": 350.00,
     *   "currency":         "USD",
     *   "paymentDate":      "2025-01-15"
     * }
     * </pre>
     *
     * @param request datos del pago confirmado
     * @return respuesta con ID del log, estado del envío y timestamp
     */
    @PostMapping("/payment-confirmation")
    @Operation(
        summary     = "Enviar confirmación de pago",
        description = "Recibe datos de un pago registrado y envía correo de confirmación al deudor. "
                    + "Llamado internamente por payment-service."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Notificación procesada",
            content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno al enviar correo")
    })
    public ResponseEntity<NotificationResponse> sendPaymentConfirmation(
            @Valid @RequestBody PaymentConfirmationRequest request) {

        log.info("POST /payment-confirmation - debtId={}, recipient={}",
                request.debtId(), request.debtorEmail());

        NotificationResponse response = notificationService.processPaymentConfirmation(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── GET /health ──────────────────────────────────────────

    /**
     * Verificación rápida de que el servicio está activo.
     * Complementa el endpoint de Actuator en {@code /actuator/health}.
     *
     * @return mensaje de estado
     */
    @GetMapping("/health")
    @Operation(summary = "Verificación de salud del servicio")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("notification-service UP");
    }
}
