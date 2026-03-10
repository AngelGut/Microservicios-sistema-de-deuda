package com.example.airiskservice.client.fallback;

import com.example.airiskservice.client.PaymentClient;
import com.example.airiskservice.dto.response.PaymentHistoryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
public class PaymentClientFallback implements PaymentClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentClientFallback.class);

    @Override
    public List<PaymentHistoryDTO> getPaymentsByClient(Long clientId) {
        log.error("Circuit breaker activo: payment-service no disponible para clientId={}", clientId);
        return Collections.emptyList();
    }
}
