package com.example.paymentservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Entidad JPA que representa un pago registrado en el sistema.
 *
 * <p>
 * Principio SRP: solo encapsula el estado persistible del pago.
 * Sin lógica de negocio aquí.
 */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Long id;

    /** ID de la deuda asociada (referencia externa a debt-service). */
    @Column(name = "debt_id", nullable = false, length = 36)
    private String debtId;

    /** Monto del pago. Nunca puede ser <= 0. */
    @Column(nullable = false)
    private BigDecimal amount;

    /** Fecha en que se realizó el pago (fecha del negocio, no de creación). */
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    /** Nota opcional del operador. */
    @Column
    private String note;

    /** Timestamp de auditoría: cuándo se registró en el sistema. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    // ── Constructores ────────────────────────────────────────

    protected Payment() {
        // Requerido por JPA
    }

    public Payment(String debtId, BigDecimal amount, LocalDate paymentDate, String note) {
        this.debtId = debtId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.note = note;
    }

    // ── Getters (solo lectura – inmutabilidad post-creación) ──

    public Long getId() {
        return id;
    }

    public String getDebtId() {
        return debtId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public String getNote() {
        return note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
