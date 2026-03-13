package com.example.notificationservice.service.impl;

import com.example.notificationservice.dto.request.PaymentConfirmationRequest;
import com.example.notificationservice.dto.response.NotificationResponse;
import com.example.notificationservice.entity.NotificationLog;
import com.example.notificationservice.repository.NotificationLogRepository;
import com.example.notificationservice.service.EmailService;
import com.example.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementación del servicio de notificaciones.
 *
 * <p>Orquesta el flujo completo:
 * <ol>
 *   <li>Delegar el envío al {@link EmailService}.</li>
 *   <li>Registrar el resultado en {@link NotificationLog}.</li>
 *   <li>Devolver una respuesta estructurada al llamador.</li>
 * </ol>
 *
 * <p>Principios SOLID aplicados:
 * <ul>
 *   <li><b>SRP</b>: solo orquesta; el envío de email y la persistencia son responsabilidades
 *       de otras clases.</li>
 *   <li><b>OCP</b>: se pueden añadir nuevas notificaciones sin modificar este flujo base.</li>
 *   <li><b>DIP</b>: depende de {@link EmailService} y {@link NotificationLogRepository}
 *       como abstracciones, no de implementaciones concretas.</li>
 * </ul>
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final EmailService emailService;
    private final NotificationLogRepository logRepository;

    public NotificationServiceImpl(
            EmailService emailService,
            NotificationLogRepository logRepository) {
        this.emailService  = emailService;
        this.logRepository = logRepository;
    }

    // ── Confirmación de pago ─────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Envía el correo de confirmación y persiste el log
     * con estado SENT. Si ocurre un error de envío, persiste
     * el log con estado FAILED y relanza la excepción para que
     * el controlador devuelva una respuesta de error apropiada.
     */
    @Override
    @Transactional
    public NotificationResponse processPaymentConfirmation(PaymentConfirmationRequest request) {
        log.info("Procesando confirmación de pago: debtId={}, recipient={}",
                request.debtId(), request.debtorEmail());

        NotificationLog.NotificationStatus status;
        String message;

        try {
            emailService.sendPaymentConfirmation(request);
            status  = NotificationLog.NotificationStatus.SENT;
            message = "Correo de confirmación enviado correctamente";
        } catch (Exception ex) {
            log.error("Error al enviar confirmación de pago para debtId={}: {}",
                    request.debtId(), ex.getMessage());
            status  = NotificationLog.NotificationStatus.FAILED;
            message = "Error al enviar correo: " + ex.getMessage();
        }

        NotificationLog saved = persistLog(
                NotificationLog.NotificationType.PAYMENT_CONFIRMATION,
                request.debtId(),
                request.debtorEmail(),
                request.debtorName(),
                status,
                message
        );

        return toResponse(saved);
    }

    // ── Métodos de uso interno (llamados por el scheduler) ───

    /**
     * Envía un recordatorio de vencimiento y persiste el resultado.
     *
     * <p>Este método es llamado por {@code ReminderScheduler} y no forma
     * parte de la interfaz pública, siguiendo el principio ISP: el
     * controlador REST no necesita conocer este método.
     *
     * @param debt   DTO de la deuda próxima a vencer
     * @param debtor DTO del deudor al que se enviará el recordatorio
     */
    @Transactional
    public void processPaymentReminder(
            com.example.notificationservice.dto.response.DebtDto debt,
            com.example.notificationservice.dto.response.DebtorDto debtor) {

        log.info("Procesando recordatorio para deuda {} - deudor: {}",
                debt.id(), debtor.email());

        NotificationLog.NotificationStatus status;
        String message;

        try {
            emailService.sendPaymentReminder(debt, debtor);
            status  = NotificationLog.NotificationStatus.SENT;
            message = "Recordatorio de vencimiento enviado correctamente";
        } catch (Exception ex) {
            log.error("Error al enviar recordatorio para deuda {}: {}", debt.id(), ex.getMessage());
            status  = NotificationLog.NotificationStatus.FAILED;
            message = "Error al enviar recordatorio: " + ex.getMessage();
        }

        persistLog(
                NotificationLog.NotificationType.PAYMENT_REMINDER,
                debt.id(),
                debtor.email(),
                debtor.name(),
                status,
                message
        );
    }

    // ── Helpers privados ─────────────────────────────────────

    private NotificationLog persistLog(
            NotificationLog.NotificationType type,
            String referenceId,
            String recipientEmail,
            String recipientName,
            NotificationLog.NotificationStatus status,
            String message) {

        NotificationLog entry = NotificationLog.builder()
                .type(type)
                .referenceId(referenceId)
                .recipientEmail(recipientEmail)
                .recipientName(recipientName)
                .status(status)
                .message(message)
                .processedAt(LocalDateTime.now())
                .build();

        return logRepository.save(entry);
    }

    private NotificationResponse toResponse(NotificationLog log) {
        return new NotificationResponse(
                log.getId(),
                log.getType().name(),
                log.getRecipientEmail(),
                log.getStatus().name(),
                log.getMessage(),
                log.getProcessedAt()
        );
    }
}
