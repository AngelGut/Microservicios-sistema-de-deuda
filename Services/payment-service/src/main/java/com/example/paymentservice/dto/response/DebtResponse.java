package com.example.paymentservice.dto.response;

import java.math.BigDecimal;

/**
 * DTO que representa la respuesta del debt-service cuando se consulta una deuda.
 *
 * <p>Solo se mapean los campos que payment-service necesita para sus validaciones.
 */
public record DebtResponse(
        String id,
        String debtorId,
        String description,
        BigDecimal originalAmount,
        BigDecimal currentBalance,
        String currency,
        String status
) {}
