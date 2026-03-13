package com.example.notificationservice.service;

import com.example.notificationservice.dto.request.PaymentConfirmationRequest;
import com.example.notificationservice.dto.response.DebtDto;
import com.example.notificationservice.dto.response.DebtorDto;

/**
 * Contrato para el servicio de envío de correos electrónicos.
 *
 * <p>Principio ISP: define operaciones cohesivas exclusivamente
 * relacionadas al envío de emails; no mezcla lógica de negocio
 * externa ni acceso a datos.
 *
 * <p>Principio DIP: las clases que necesiten enviar correos
 * dependen de esta abstracción, no de la implementación concreta.
 */
public interface EmailService {

    /**
     * Envía un correo de confirmación de pago al deudor.
     *
     * @param request datos del pago confirmado, incluyendo destinatario y montos
     */
    void sendPaymentConfirmation(PaymentConfirmationRequest request);

    /**
     * Envía un correo recordatorio de vencimiento próximo al deudor.
     *
     * @param debt   datos de la deuda próxima a vencer
     * @param debtor datos del deudor destinatario
     */
    void sendPaymentReminder(DebtDto debt, DebtorDto debtor);
}
