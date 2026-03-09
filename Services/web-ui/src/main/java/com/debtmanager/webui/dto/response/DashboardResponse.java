package com.debtmanager.webui.dto.response;

import java.math.BigDecimal;

public record DashboardResponse(
        long totalDebtors,
        long activeDebts,
        long paymentsThisMonth,
        BigDecimal usdToDopRate) {
}
