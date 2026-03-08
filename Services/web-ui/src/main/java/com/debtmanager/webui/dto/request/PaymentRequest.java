package com.debtmanager.webui.dto.request;

import java.math.BigDecimal;

public record PaymentRequest(
        String debtId,
        BigDecimal amount,
        String reference,
        String notes) {
}
