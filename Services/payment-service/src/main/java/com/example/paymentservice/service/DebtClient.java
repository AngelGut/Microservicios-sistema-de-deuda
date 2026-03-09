package com.example.paymentservice.service;

import com.example.paymentservice.dto.response.DebtResponse;

import java.util.Optional;

/**
 * Contrato del cliente HTTP hacia debt-service.
 *
 * <p>Principio DIP: PaymentServiceImpl depende de esta abstracción,
 * no de la implementación concreta de RestTemplate/WebClient.
 * Esto facilita el testeo con mocks.
 */
public interface DebtClient {

    /**
     * Consulta una deuda por ID en debt-service.
     *
     * @param debtId ID de la deuda a consultar
     * @return {@link Optional} con la deuda si se encontró, vacío si no existe
     *         o si debt-service no responde (fallback de Resilience4j).
     */
    Optional<DebtResponse> findById(Long debtId);

    /**
     * Notifica a debt-service que se registró un pago para una deuda,
     * para que actualice el saldo y estado.
     *
     * @param debtId ID de la deuda
     * @param paidAmount monto que se acaba de pagar
     */
    void notifyPayment(Long debtId, java.math.BigDecimal paidAmount);
}
