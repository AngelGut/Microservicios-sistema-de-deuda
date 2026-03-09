package com.example.airiskservice.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entidad JPA que representa el perfil de riesgo crediticio de un cliente.
 *
 * Principio SRP: solo encapsula el estado persistible del riesgo.
 */
@Entity
@Table(name = "client_risk")
public class ClientRisk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Long id;

    @Column(name = "client_id", nullable = false, unique = true)
    private Long clientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    /** Puntuación de riesgo entre 0.0 (sin riesgo) y 100.0 (máximo riesgo). */
    @Column(name = "risk_score", nullable = false)
    private Double riskScore;

    @Column(name = "total_days_late", nullable = false)
    private Integer totalDaysLate;

    @Column(name = "late_payment_count", nullable = false)
    private Integer latePaymentCount;

    @Column(name = "payment_count", nullable = false)
    private Integer paymentCount;

    @Column(name = "last_calculated_at", nullable = false)
    private Instant lastCalculatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    protected ClientRisk() {}

    public ClientRisk(Long clientId) {
        this.clientId = clientId;
        this.riskLevel = RiskLevel.GOOD_CLIENT;
        this.riskScore = 0.0;
        this.totalDaysLate = 0;
        this.latePaymentCount = 0;
        this.paymentCount = 0;
        this.lastCalculatedAt = Instant.now();
    }

    // ── Getters ──────────────────────────────────────────────
    public Long getId()                  { return id; }
    public Long getClientId()            { return clientId; }
    public RiskLevel getRiskLevel()      { return riskLevel; }
    public Double getRiskScore()         { return riskScore; }
    public Integer getTotalDaysLate()    { return totalDaysLate; }
    public Integer getLatePaymentCount() { return latePaymentCount; }
    public Integer getPaymentCount()     { return paymentCount; }
    public Instant getLastCalculatedAt() { return lastCalculatedAt; }
    public Instant getCreatedAt()        { return createdAt; }
    public Instant getUpdatedAt()        { return updatedAt; }

    // ── Setters (solo los campos que cambian en recálculo) ───
    public void setRiskLevel(RiskLevel riskLevel)          { this.riskLevel = riskLevel; }
    public void setRiskScore(Double riskScore)             { this.riskScore = riskScore; }
    public void setTotalDaysLate(Integer totalDaysLate)    { this.totalDaysLate = totalDaysLate; }
    public void setLatePaymentCount(Integer count)         { this.latePaymentCount = count; }
    public void setPaymentCount(Integer paymentCount)      { this.paymentCount = paymentCount; }
    public void setLastCalculatedAt(Instant lastCalc)      { this.lastCalculatedAt = lastCalc; }
}
