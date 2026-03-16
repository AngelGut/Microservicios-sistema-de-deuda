package com.example.notificationservice.service.impl;

import com.example.notificationservice.dto.response.DebtorDto;
import com.example.notificationservice.service.DebtorClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Implementación del cliente HTTP para {@code debtor-service}.
 *
 * <p>Principio SRP: responsabilidad única de recuperar datos
 * de deudores desde el servicio remoto.
 *
 * <p>Protegido con Circuit Breaker y Retry para manejar
 * fallos temporales del servicio remoto.
 */
@Service
public class DebtorClientImpl implements DebtorClient {

    private static final Logger log = LoggerFactory.getLogger(DebtorClientImpl.class);

    private final RestTemplate restTemplate;
    private final String debtorServiceBaseUrl;

    public DebtorClientImpl(
            RestTemplate restTemplate,
            @Value("${app.debtor-service.base-url}") String debtorServiceBaseUrl) {
        this.restTemplate        = restTemplate;
        this.debtorServiceBaseUrl = debtorServiceBaseUrl;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Llama a {@code GET /api/v1/debtors/{id}} en debtor-service.
     */
    @Override
    @CircuitBreaker(name = "debtorClient", fallbackMethod = "findByIdFallback")
    @Retry(name = "debtorClient")
    public Optional<DebtorDto> findById(String debtorId) {
        String url = debtorServiceBaseUrl + "/api/v1/debtors/" + debtorId;
        try {
            ResponseEntity<ApiResponse<DebtorDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<DebtorDto>>() {}
            );
            if (response.getBody() != null && response.getBody().data() != null) {
                return Optional.of(response.getBody().data());
            }
            return Optional.empty();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Deudor {} no encontrado en debtor-service", debtorId);
            return Optional.empty();
        }
    }

    // ── Fallback ─────────────────────────────────────────────

    @SuppressWarnings("unused")
    private Optional<DebtorDto> findByIdFallback(String debtorId, Throwable t) {
        log.warn("Circuit Breaker activo para debtorClient.findById({}): {}", debtorId, t.getMessage());
        return Optional.empty();
    }

    // ── Wrapper genérico ─────────────────────────────────────

    private record ApiResponse<T>(
            String traceId,
            boolean success,
            T data,
            Object error
    ) {}
}
