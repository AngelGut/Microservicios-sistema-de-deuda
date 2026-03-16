package com.debtmanager.webui.dto.response;

import java.util.List;

public record AiRiskResponse(
        String status,
        String explanation,
        Double confidence,
        Double riskScore,
        Integer totalDaysLate,
        Integer latePaymentCount,
        Integer paymentCount,
        List<String> aiRecommendations) {
}
