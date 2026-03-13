package com.example.airiskservice.client.fallback;

import com.example.airiskservice.client.PaymentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class PaymentClientFallback implements PaymentClient {
    private static final Logger log = LoggerFactory.getLogger(PaymentClientFallback.class);

    @Override
    public PaymentListResponse getPaymentsByDebt(String debtId) {
        log.error("Circuit breaker activo: payment-service no disponible para debtId={}", debtId);
        return new PaymentListResponse(List.of());
    }
}
