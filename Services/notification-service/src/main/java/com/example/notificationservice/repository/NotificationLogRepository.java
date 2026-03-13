package com.example.notificationservice.repository;

import com.example.notificationservice.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para el acceso a datos de {@link NotificationLog}.
 *
 * <p>Principio SRP: encapsula únicamente la lógica de acceso a
 * datos para los registros de notificación.
 *
 * <p>Principio DIP: el resto del sistema depende de esta
 * abstracción, no de una implementación concreta de persistencia.
 */
@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    /**
     * Verifica si ya existe un registro de notificación exitosa del tipo
     * indicado para la referencia dada dentro de un rango de tiempo.
     *
     * <p>Útil para el scheduler: evita enviar recordatorios duplicados
     * si la tarea se ejecuta varias veces en el mismo día.
     *
     * @param type        tipo de notificación a verificar
     * @param referenceId ID de la deuda o pago de referencia
     * @param status      estado requerido (ej. SENT)
     * @param after       límite inferior del rango temporal
     * @return {@code true} si ya se envió una notificación con esos parámetros
     */
    boolean existsByTypeAndReferenceIdAndStatusAndProcessedAtAfter(
            NotificationLog.NotificationType type,
            String referenceId,
            NotificationLog.NotificationStatus status,
            LocalDateTime after
    );

    /**
     * Obtiene todos los registros de un destinatario, ordenados
     * del más reciente al más antiguo.
     *
     * @param recipientEmail correo del destinatario
     * @return lista de logs ordenados por fecha descendente
     */
    List<NotificationLog> findByRecipientEmailOrderByProcessedAtDesc(String recipientEmail);
}
