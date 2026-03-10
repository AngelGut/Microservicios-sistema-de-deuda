package com.example.airiskservice.dto.response;

import com.example.airiskservice.model.RiskLevel;

public record RiskCalculationResult(
        Long clientId,
        RiskLevel riskLevel,
        Double riskScore,
        Integer totalDaysLate,
        Integer latePaymentCount,
        Integer paymentCount
) {}
