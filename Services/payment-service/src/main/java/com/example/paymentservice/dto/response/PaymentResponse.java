package com.example.paymentservice.dto.response;

import com.example.paymentservice.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de salida que representa un pago para el cliente.
 *
 * <p>Principio SRP: transforma el modelo de dominio en una vista
 * segura y serializable, sin exponer detalles de persistencia.
 */
public record PaymentResponse(
        Long id,
        String debtId,
        BigDecimal amount,
        LocalDate paymentDate,
        String note,
        LocalDateTime createdAt
) {
    /**
     * Factory method: convierte una entidad {@link Payment} en su DTO de respuesta.
     */
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getDebtId(),
                payment.getAmount(),
                payment.getPaymentDate(),
                payment.getNote(),
                payment.getCreatedAt()
        );
    }
}
