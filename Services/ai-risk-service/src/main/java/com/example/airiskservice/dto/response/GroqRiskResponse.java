package com.example.airiskservice.dto.response;

import com.example.airiskservice.model.RiskLevel;
import java.util.List;

/**
 * Resultado del análisis de riesgo realizado por Groq AI.
 * SRP: solo representa la respuesta de la IA.
 */
public record GroqRiskResponse(
        RiskLevel riskLevel,
        Double aiScore,
        List<String> recommendations,
        String rawAnalysis
) {}
