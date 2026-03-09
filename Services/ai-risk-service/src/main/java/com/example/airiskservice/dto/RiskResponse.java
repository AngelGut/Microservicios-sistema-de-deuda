package com.example.airiskservice.dto;

import com.example.airiskservice.model.ClientRisk;
import com.example.airiskservice.model.RiskLevel;

import java.time.Instant;

/**
 * DTO de salida con el perfil de riesgo de un cliente.
 *
 * <p>Principio SRP: transforma la entidad en una vista segura para el cliente.
 */
public record RiskResponse(
        Long clientId,
        RiskLevel riskLevel,
        Double riskScore,
        Integer totalDaysLate,
        Integer latePaymentCount,
        Integer paymentCount,
        Instant lastCalculatedAt
) {
    public static RiskResponse from(ClientRisk risk) {
        return new RiskResponse(
                risk.getClientId(),
                risk.getRiskLevel(),
                risk.getRiskScore(),
                risk.getTotalDaysLate(),
                risk.getLatePaymentCount(),
                risk.getPaymentCount(),
                risk.getLastCalculatedAt()
        );
    }
}
