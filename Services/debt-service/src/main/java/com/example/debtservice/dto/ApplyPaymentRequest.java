package com.example.debtservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO que representa el monto a aplicar como pago a una deuda.
 * Es llamado internamente por el payment-service.
 */
@Data
public class ApplyPaymentRequest {

    /** Monto del pago a aplicar, debe ser mayor a 0 */
    @NotNull(message = "El monto del pago es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal amount;
}
