package com.example.airiskservice.client.fallback;

import com.example.airiskservice.client.DebtClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DebtClientFallback implements DebtClient {
    private static final Logger log = LoggerFactory.getLogger(DebtClientFallback.class);

    @Override
    public DebtListResponse getDebtsByDebtor(String debtorId) {
        log.error("Circuit breaker activo: debt-service no disponible para debtorId={}", debtorId);
        return new DebtListResponse(List.of());
    }
}
