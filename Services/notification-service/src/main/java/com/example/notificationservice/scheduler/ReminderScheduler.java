package com.example.notificationservice.scheduler;

import com.example.notificationservice.dto.response.DebtDto;
import com.example.notificationservice.dto.response.DebtorDto;
import com.example.notificationservice.entity.NotificationLog;
import com.example.notificationservice.repository.NotificationLogRepository;
import com.example.notificationservice.service.DebtClient;
import com.example.notificationservice.service.DebtorClient;
import com.example.notificationservice.service.impl.NotificationServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Tarea programada que revisa diariamente las deudas próximas a vencer
 * y envía recordatorios por correo a los deudores correspondientes.
 *
 * <p>Se ejecuta cada día a las 08:00 (configurable vía cron).
 * Para cada deuda activa con fecha de vencimiento en exactamente N días
 * (por defecto 3), verifica que no se haya enviado ya un recordatorio hoy
 * y, si aplica, delega el envío a {@link NotificationServiceImpl}.
 *
 * <p>Principio SRP: su única responsabilidad es iterar las deudas
 * y coordinar el envío. El envío real y la persistencia del log son
 * responsabilidad de {@link NotificationServiceImpl}.
 *
 * <p>Principio DIP: depende de las interfaces {@link DebtClient},
 * {@link DebtorClient} y {@link NotificationServiceImpl}, no de
 * implementaciones concretas de HTTP.
 */
@Component
public class ReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final DebtClient               debtClient;
    private final DebtorClient             debtorClient;
    private final NotificationServiceImpl  notificationService;
    private final NotificationLogRepository logRepository;

    @Value("${app.notification.reminder-days-before:3}")
    private int reminderDaysBefore;

    public ReminderScheduler(
            DebtClient debtClient,
            DebtorClient debtorClient,
            NotificationServiceImpl notificationService,
            NotificationLogRepository logRepository) {
        this.debtClient          = debtClient;
        this.debtorClient        = debtorClient;
        this.notificationService = notificationService;
        this.logRepository       = logRepository;
    }

    /**
     * Punto de entrada del scheduler.
     *
     * <p>Cron: todos los días a las 08:00 AM.
     * Puede ajustarse con la propiedad {@code app.notification.reminder-cron}
     * sin necesidad de recompilar.
     *
     * <p>Flujo:
     * <ol>
     *   <li>Calcular la fecha objetivo ({@code hoy + reminderDaysBefore}).</li>
     *   <li>Consultar en debt-service las deudas ACTIVAS con esa fecha.</li>
     *   <li>Para cada deuda, verificar que no se haya enviado ya el recordatorio hoy.</li>
     *   <li>Recuperar los datos del deudor desde debtor-service.</li>
     *   <li>Validar que el deudor tiene email registrado.</li>
     *   <li>Delegar el envío a {@link NotificationServiceImpl#processPaymentReminder}.</li>
     * </ol>
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendPaymentReminders() {
        LocalDate targetDate = LocalDate.now().plusDays(reminderDaysBefore);
        log.info("Scheduler: buscando deudas con vencimiento el {} ({} días)",
                targetDate, reminderDaysBefore);

        List<DebtDto> upcomingDebts = debtClient.findActiveDebtsWithDueDate(targetDate);

        if (upcomingDebts.isEmpty()) {
            log.info("Scheduler: no hay deudas próximas a vencer para {}", targetDate);
            return;
        }

        log.info("Scheduler: {} deuda(s) encontrada(s) con vencimiento el {}",
                upcomingDebts.size(), targetDate);

        int sent   = 0;
        int skipped = 0;
        int errors  = 0;

        for (DebtDto debt : upcomingDebts) {
            try {
                // Evitar recordatorios duplicados en el mismo día
                if (alreadySentTodayReminder(debt.id())) {
                    log.debug("Scheduler: recordatorio ya enviado hoy para deuda {}", debt.id());
                    skipped++;
                    continue;
                }

                // Recuperar datos del deudor
                Optional<DebtorDto> debtorOpt = debtorClient.findById(debt.debtorId());
                if (debtorOpt.isEmpty()) {
                    log.warn("Scheduler: deudor {} no encontrado para deuda {}",
                            debt.debtorId(), debt.id());
                    errors++;
                    continue;
                }

                DebtorDto debtor = debtorOpt.get();

                // Validar que el deudor tiene email
                if (debtor.email() == null || debtor.email().isBlank()) {
                    log.warn("Scheduler: deudor {} no tiene email registrado, omitiendo deuda {}",
                            debtor.id(), debt.id());
                    skipped++;
                    continue;
                }

                notificationService.processPaymentReminder(debt, debtor);
                sent++;

            } catch (Exception ex) {
                log.error("Scheduler: error procesando recordatorio para deuda {}: {}",
                        debt.id(), ex.getMessage(), ex);
                errors++;
            }
        }

        log.info("Scheduler completado: enviados={}, omitidos={}, errores={}", sent, skipped, errors);
    }

    // ── Helpers privados ─────────────────────────────────────

    /**
     * Verifica si ya se envió un recordatorio exitoso para esta deuda
     * en las últimas 24 horas.
     *
     * @param debtId ID de la deuda
     * @return {@code true} si ya fue notificada hoy
     */
    private boolean alreadySentTodayReminder(String debtId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return logRepository.existsByTypeAndReferenceIdAndStatusAndProcessedAtAfter(
                NotificationLog.NotificationType.PAYMENT_REMINDER,
                debtId,
                NotificationLog.NotificationStatus.SENT,
                startOfDay
        );
    }
}
