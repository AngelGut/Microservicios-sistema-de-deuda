package com.example.airiskservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DebtDTO(
        String id,
        String debtorId,
        String description,
        BigDecimal originalAmount,
        BigDecimal currentBalance,
        String currency,
        String status,
        LocalDate dueDate) {
}
