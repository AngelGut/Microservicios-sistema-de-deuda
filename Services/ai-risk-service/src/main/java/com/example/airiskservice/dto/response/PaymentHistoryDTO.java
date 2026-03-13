package com.example.airiskservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentHistoryDTO(
        String id,
        String debtId,
        String clientId,
        BigDecimal amount,
        LocalDate paymentDate,
        LocalDate dueDate,
        String note
) {}
