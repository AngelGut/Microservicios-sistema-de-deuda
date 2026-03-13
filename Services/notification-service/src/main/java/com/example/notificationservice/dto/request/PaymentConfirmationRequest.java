package com.example.notificationservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de entrada que representa el evento de confirmación de pago.
 *
 * <p>Este objeto es enviado por {@code payment-service} al endpoint
 * {@code POST /api/v1/notifications/payment-confirmation}
 * una vez que un pago ha sido registrado exitosamente.
 *
 * <p>Principio SRP: solo encapsula los datos necesarios para
 * construir el correo de confirmación de pago.
 *
 * <p>Ejemplo de JSON recibido:
 * <pre>
 * {
 *   "debtId":         "uuid-deuda-001",
 *   "debtorId":       "42",
 *   "debtorName":     "Juan Pérez",
 *   "debtorEmail":    "juan.perez@email.com",
 *   "amountPaid":     150.00,
 *   "remainingBalance": 350.00,
 *   "currency":       "USD",
 *   "paymentDate":    "2025-01-15"
 * }
 * </pre>
 */
public record PaymentConfirmationRequest(

        /** ID único de la deuda asociada al pago. */
        @NotBlank(message = "El ID de la deuda es obligatorio")
        String debtId,

        /** ID del deudor en debtor-service. */
        @NotBlank(message = "El ID del deudor es obligatorio")
        String debtorId,

        /** Nombre completo del deudor para personalizar el saludo. */
        @NotBlank(message = "El nombre del deudor es obligatorio")
        String debtorName,

        /** Dirección de correo electrónico del deudor. */
        @NotBlank(message = "El email del deudor es obligatorio")
        String debtorEmail,

        /** Monto del pago realizado. */
        @NotNull(message = "El monto pagado es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        BigDecimal amountPaid,

        /** Balance restante de la deuda tras aplicar el pago. */
        @NotNull(message = "El balance restante es obligatorio")
        BigDecimal remainingBalance,

        /** Moneda del pago (ej. USD, DOP). */
        @NotBlank(message = "La moneda es obligatoria")
        String currency,

        /** Fecha en que se realizó el pago. */
        @NotNull(message = "La fecha del pago es obligatoria")
        LocalDate paymentDate
) {}
