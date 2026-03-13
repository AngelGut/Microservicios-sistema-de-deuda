package com.example.paymentservice.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de entrada para registrar un nuevo pago.
 *
 * <p>Principio SRP: solo transfiere y valida los datos del request.
 * No contiene lógica de negocio.
 */
public record CreatePaymentRequest(

        @NotBlank(message = "El ID de la deuda es obligatorio")
        String debtId,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero")
        @Digits(integer = 15, fraction = 2, message = "El monto no puede tener más de 2 decimales")
        BigDecimal amount,

        @NotNull(message = "La fecha de pago es obligatoria")
        LocalDate paymentDate,

        @Size(max = 255, message = "La nota no puede exceder 255 caracteres")
        String note
) {}
