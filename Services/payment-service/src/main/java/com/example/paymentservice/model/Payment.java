package com.example.paymentservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Long id;

    @Column(name = "debt_id", nullable = false, length = 36)
    private String debtId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TEXT")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    protected Payment() {
    }

    public Payment(String debtId, BigDecimal amount, LocalDate paymentDate, String note) {
        this.debtId = debtId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.note = note;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}