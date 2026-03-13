package com.example.airiskservice.client;

import com.example.airiskservice.dto.response.GroqRiskResponse;
import com.example.airiskservice.dto.response.PaymentHistoryDTO;
import java.util.List;

public interface GroqAiAnalyzer {

    GroqRiskResponse analyze(String clientId,
            int totalDaysLate,
            int latePaymentCount,
            int paymentCount,
            List<PaymentHistoryDTO> payments);
}
