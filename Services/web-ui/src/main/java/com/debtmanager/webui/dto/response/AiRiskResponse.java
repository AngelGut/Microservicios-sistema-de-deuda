package com.debtmanager.webui.dto.response;

/**
 * Respuesta del ai-risk-service.
 * GET /api/v1/ai/risk/debtor/{id}
 *
 * status: FIABLE | REVISION | BLOQUEADO | NO_DISPONIBLE
 */
public record AiRiskResponse(
        String status,
        String explanation,
        Double confidence) {
}
