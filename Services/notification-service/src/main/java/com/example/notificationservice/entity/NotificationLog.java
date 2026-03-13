package com.example.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que registra el historial de todas las notificaciones
 * enviadas o intentadas por el servicio.
 *
 * <p>Permite auditar qué correos fueron enviados, a quién,
 * cuándo y con qué resultado. Es útil para evitar envíos
 * duplicados y para diagnóstico de fallos.
 *
 * <p>Principio SRP: solo representa el log de auditoría de
 * notificaciones; no contiene lógica de negocio.
 */
@Entity
@Table(name = "notification_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tipo de notificación enviada.
     *
     * @see NotificationType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    /** ID de referencia de la entidad origen (deuda o pago). */
    @Column(name = "reference_id", length = 50)
    private String referenceId;

    /** Correo electrónico del destinatario. */
    @Column(name = "recipient_email", nullable = false, length = 150)
    private String recipientEmail;

    /** Nombre del destinatario para auditoría. */
    @Column(name = "recipient_name", length = 150)
    private String recipientName;

    /**
     * Estado del envío.
     *
     * @see NotificationStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private NotificationStatus status;

    /** Mensaje de éxito o detalle del error. */
    @Column(name = "message", length = 500)
    private String message;

    /** Momento en que se procesó la notificación. */
    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    /** Momento de creación del registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (processedAt == null) {
            processedAt = LocalDateTime.now();
        }
    }

    // ── Enumeraciones internas ───────────────────────────────

    public enum NotificationType {
        PAYMENT_CONFIRMATION,
        PAYMENT_REMINDER
    }

    public enum NotificationStatus {
        SENT,
        FAILED
    }
}
