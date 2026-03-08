package com.debtmanager.webui.dto.response;

import java.math.BigDecimal;

public record DebtResponse(
        String id,
        String debtorId,
        String description,
        BigDecimal originalAmount,
        BigDecimal currentBalance,
        String currency,
        String status,
        String dueDate) {
}
