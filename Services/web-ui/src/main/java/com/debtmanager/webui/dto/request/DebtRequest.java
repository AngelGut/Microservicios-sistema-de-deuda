package com.debtmanager.webui.dto.request;

import java.math.BigDecimal;

public record DebtRequest(
        String debtorId,
        String description,
        BigDecimal originalAmount,
        String currency,
        String dueDate) {
}
