package com.debtmanager.webui.dto.response;

import java.math.BigDecimal;

/**
 * Pagos recientes para el dashboard (actividad + gráfico de barras).
 * GET /api/v1/payments/recent?limit=7
 *
 * Endpoint que debe exponer payment-service.
 */
public record RecentPaymentResponse(
        String id,
        String debtId,
        String debtorName, // enriquecido por payment-service o puede ser null
        BigDecimal amount,
        String currency,
        String reference,
        String status,
        String paidAt // ISO datetime
) {
}
