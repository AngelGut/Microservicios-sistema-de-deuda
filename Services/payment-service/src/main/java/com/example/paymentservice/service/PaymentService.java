package com.example.paymentservice.service;

import com.example.paymentservice.dto.request.CreatePaymentRequest;
import com.example.paymentservice.dto.response.PaymentResponse;

import java.util.List;

/**
 * Contrato del servicio de pagos.
 *
 * <p>Principio ISP: interfaz enfocada en las operaciones de negocio del dominio.
 * Principio DIP: los controladores dependen de esta abstracción, no de la implementación concreta.
 */
public interface PaymentService {

    /**
     * Registra un nuevo pago.
     * Valida que la deuda exista, que no esté completamente pagada
     * y que el monto no supere el saldo pendiente.
     *
     * @param request datos del pago a registrar
     * @return pago registrado como DTO de respuesta
     */
    PaymentResponse createPayment(CreatePaymentRequest request);

    /**
     * Devuelve todos los pagos del sistema.
     *
     * @return lista de pagos
     */
    List<PaymentResponse> getAllPayments();

    /**
     * Devuelve todos los pagos asociados a una deuda específica.
     *
     * @param debtId ID de la deuda (UUID String)
     * @return lista de pagos de esa deuda
     */
    List<PaymentResponse> getPaymentsByDebt(String debtId);

    /**
     * Devuelve un pago por su ID.
     *
     * @param id ID del pago
     * @return pago encontrado como DTO de respuesta
     */
    PaymentResponse getPaymentById(Long id);
}
