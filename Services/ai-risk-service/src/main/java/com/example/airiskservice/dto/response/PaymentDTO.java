package com.example.airiskservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PaymentDTO(
        Long id,
        String debtId,
        BigDecimal amount,
        LocalDate paymentDate,
        String note,
        LocalDateTime createdAt) {
}
