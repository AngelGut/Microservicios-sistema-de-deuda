package com.example.airiskservice.client.fallback;

import com.example.airiskservice.client.DebtClient;
import com.example.common.api.ApiResponse;
import com.example.airiskservice.dto.response.DebtDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
public class DebtClientFallback implements DebtClient {

    private static final Logger log = LoggerFactory.getLogger(DebtClientFallback.class);

    @Override
    public ApiResponse<List<DebtDTO>> getDebtsByDebtor(String debtorId) {
        log.error("Circuit breaker activo: debt-service no disponible para debtorId={}", debtorId);
        return ApiResponse.ok(Collections.emptyList(), "fallback");
    }
}
