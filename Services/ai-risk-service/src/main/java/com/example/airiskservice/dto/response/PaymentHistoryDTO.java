package com.example.airiskservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentHistoryDTO(
        Long id,
        Long debtId,
        Long clientId,
        BigDecimal amount,
        LocalDate paymentDate,
        LocalDate dueDate,
        String note
) {}
