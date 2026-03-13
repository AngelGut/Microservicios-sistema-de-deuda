package com.example.airiskservice.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "client_risk")
public class ClientRisk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Long id;

    @Column(name = "client_id", nullable = false, unique = true)
    private String clientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Column(name = "risk_score", nullable = false)
    private Double riskScore;

    @Column(name = "total_days_late", nullable = false)
    private Integer totalDaysLate;

    @Column(name = "late_payment_count", nullable = false)
    private Integer latePaymentCount;

    @Column(name = "payment_count", nullable = false)
    private Integer paymentCount;

    @Column(name = "last_calculated_at", nullable = false)
    private String lastCalculatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private String createdAt;

    @Column(name = "updated_at", nullable = false)
    private String updatedAt;

    @PrePersist
    protected void onCreate() {
        String now = Instant.now().toString();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now().toString();
    }

    protected ClientRisk() {
    }

    public ClientRisk(String clientId) {
        this.clientId = clientId;
        this.riskLevel = RiskLevel.GOOD_CLIENT;
        this.riskScore = 0.0;
        this.totalDaysLate = 0;
        this.latePaymentCount = 0;
        this.paymentCount = 0;
        this.lastCalculatedAt = Instant.now().toString();
    }

    // ── Getters ──────────────────────────────────────────────
    public Long getId() {
        return id;
    }

    public String getClientId() {
        return clientId;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public Integer getTotalDaysLate() {
        return totalDaysLate;
    }

    public Integer getLatePaymentCount() {
        return latePaymentCount;
    }

    public Integer getPaymentCount() {
        return paymentCount;
    }

    public Instant getLastCalculatedAt() {
        return Instant.parse(lastCalculatedAt);
    }

    public Instant getCreatedAt() {
        return Instant.parse(createdAt);
    }

    public Instant getUpdatedAt() {
        return Instant.parse(updatedAt);
    }

    // ── Setters ──────────────────────────────────────────────
    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public void setTotalDaysLate(Integer totalDaysLate) {
        this.totalDaysLate = totalDaysLate;
    }

    public void setLatePaymentCount(Integer count) {
        this.latePaymentCount = count;
    }

    public void setPaymentCount(Integer paymentCount) {
        this.paymentCount = paymentCount;
    }

    public void setLastCalculatedAt(Instant lastCalc) {
        this.lastCalculatedAt = lastCalc.toString();
    }
}
