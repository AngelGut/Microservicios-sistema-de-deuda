package com.debtmanager.webui.dto.response;

import java.math.BigDecimal;

/**
 * Resumen de deudas para el dashboard.
 * GET /api/v1/debts/summary
 *
 * Endpoint que debe exponer debt-service.
 * Si no está disponible, DashboardService retorna este record con valores en
 * cero.
 */
public record DebtSummaryResponse(
        long totalActivas,
        long totalPagadas,
        BigDecimal montoTotalActivo, // suma de currentBalance de deudas ACTIVAS
        BigDecimal montoTotalCobrado // suma de todos los pagos aplicados
) {
}
