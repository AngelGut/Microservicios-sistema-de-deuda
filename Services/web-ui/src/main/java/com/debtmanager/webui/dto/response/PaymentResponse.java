package com.debtmanager.webui.dto.response;

import java.math.BigDecimal;

public record PaymentResponse(
        String id,
        String debtId,
        BigDecimal amount,
        String reference,
        String notes,
        String status,
        String paidAt) {
}
