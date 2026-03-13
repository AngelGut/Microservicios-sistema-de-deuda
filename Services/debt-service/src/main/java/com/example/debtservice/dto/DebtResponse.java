package com.example.debtservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO que representa la respuesta al cliente cuando consulta una deuda.
 * Evita exponer directamente la entidad de base de datos.
 */
@Data
@Builder
public class DebtResponse {

    /** ID único de la deuda */
    private String id;

    /** ID del deudor */
    private String debtorId;

    /** Descripción de la deuda */
    private String description;

    /** Monto original de la deuda */
    private BigDecimal originalAmount;

    /** Balance actual pendiente de pago */
    private BigDecimal currentBalance;

    /** Moneda: USD o DOP */
    private String currency;

    /** Estado: ACTIVA o PAGADA */
    private String status;

    /** Fecha límite de pago */
    private LocalDate dueDate;

    /** Fecha de creación */
    private LocalDateTime createdAt;

    /** Fecha de última actualización */
    private LocalDateTime updatedAt;
}
