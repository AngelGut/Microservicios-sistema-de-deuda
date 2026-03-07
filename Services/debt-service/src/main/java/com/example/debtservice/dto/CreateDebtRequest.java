package com.example.debtservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO que representa los datos necesarios para crear una nueva deuda.
 * Solo contiene los campos que el cliente debe enviar, no todos los campos de
 * la entidad.
 */
@Data
public class CreateDebtRequest {

    /** ID del deudor al que pertenece esta deuda */
    @NotBlank(message = "El ID del deudor es obligatorio")
    private String debtorId;

    /** Descripción de la deuda */
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 300, message = "La descripción no puede superar 300 caracteres")
    private String description;

    /** Monto original de la deuda, debe ser mayor a 0 */
    @NotNull(message = "El monto original es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal originalAmount;

    /** Moneda: USD o DOP */
    @NotBlank(message = "La moneda es obligatoria")
    @Pattern(regexp = "USD|DOP", message = "La moneda debe ser USD o DOP")
    private String currency;

    /** Fecha límite de pago, opcional */
    private LocalDate dueDate;
}
