package com.example.airiskservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO que representa un pago recibido desde payment-service.
 *
 * <p>Solo se mapean los campos necesarios para el cálculo de riesgo.
 */
public record PaymentHistoryDTO(
        Long id,
        Long debtId,
        BigDecimal amount,
        LocalDate paymentDate,
        LocalDate dueDate,       // fecha de vencimiento de la deuda
        String note
) {
    /**
     * Calcula los días de mora de este pago.
     * Si se pagó antes o en fecha, retorna 0.
     */
    public int daysLate() {
        if (dueDate == null || paymentDate == null) return 0;
        long diff = java.time.temporal.ChronoUnit.DAYS.between(dueDate, paymentDate);
        return (int) Math.max(0, diff);
    }
}
