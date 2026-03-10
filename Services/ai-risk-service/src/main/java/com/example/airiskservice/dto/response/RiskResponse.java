package com.example.airiskservice.dto.response;

import com.example.airiskservice.model.ClientRisk;
import com.example.airiskservice.model.RiskLevel;
import java.time.Instant;
import java.util.List;

/**
 * DTO de salida con resultado combinado: reglas + análisis IA.
 */
public record RiskResponse(
        Long clientId,
        // ── resultado de reglas ──
        RiskLevel riskLevel,
        Double riskScore,
        Integer totalDaysLate,
        Integer latePaymentCount,
        Integer paymentCount,
        Instant lastCalculatedAt,
        // ── resultado IA (null si Groq no disponible) ──
        RiskLevel aiRiskLevel,
        Double aiScore,
        List<String> aiRecommendations
) {
    public static RiskResponse from(ClientRisk risk) {
        return new RiskResponse(risk.getClientId(), risk.getRiskLevel(),
                risk.getRiskScore(), risk.getTotalDaysLate(),
                risk.getLatePaymentCount(), risk.getPaymentCount(),
                risk.getLastCalculatedAt(), null, null, null);
    }

    public static RiskResponse from(ClientRisk risk, GroqRiskResponse ai) {
        return new RiskResponse(risk.getClientId(), risk.getRiskLevel(),
                risk.getRiskScore(), risk.getTotalDaysLate(),
                risk.getLatePaymentCount(), risk.getPaymentCount(),
                risk.getLastCalculatedAt(),
                ai != null ? ai.riskLevel() : null,
                ai != null ? ai.aiScore() : null,
                ai != null ? ai.recommendations() : null);
    }
}
