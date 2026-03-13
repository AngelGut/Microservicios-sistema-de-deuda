package com.example.notificationservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO que representa los datos de una deuda obtenidos desde {@code debt-service}.
 *
 * <p>Mapea la respuesta del endpoint:
 * {@code GET /api/v1/debts/{id}} de debt-service
 *
 * <p>El scheduler de recordatorios usa este DTO para consultar
 * las deudas próximas a vencer y extraer el email del deudor
 * a través del campo {@code debtorId}.
 */
public record DebtDto(

        /** ID único de la deuda (UUID). */
        String id,

        /** ID del deudor al que pertenece la deuda. */
        String debtorId,

        /** Descripción o concepto de la deuda. */
        String description,

        /** Monto original de la deuda al momento de crearla. */
        BigDecimal originalAmount,

        /** Balance actual pendiente de pago. */
        BigDecimal currentBalance,

        /** Moneda (USD, DOP, etc.). */
        String currency,

        /** Estado: ACTIVA, PAGADA, VENCIDA. */
        String status,

        /** Fecha límite de pago. */
        LocalDate dueDate,

        /** Fecha de creación de la deuda. */
        LocalDateTime createdAt
) {}
