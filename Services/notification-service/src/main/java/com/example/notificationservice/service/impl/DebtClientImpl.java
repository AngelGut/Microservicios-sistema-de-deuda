package com.example.notificationservice.service.impl;

import com.example.notificationservice.dto.response.DebtDto;
import com.example.notificationservice.service.DebtClient;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del cliente HTTP para {@code debt-service}.
 *
 * <p>Utiliza {@link RestTemplate} con Circuit Breaker y Retry de Resilience4j
 * para garantizar resiliencia ante fallos del servicio remoto.
 *
 * <p>Principio SRP: solo gestiona la comunicación HTTP con debt-service;
 * no contiene lógica de negocio.
 *
 * <p>Principio DIP: expone la interfaz {@link DebtClient}, no la implementación.
 */
@Service
public class DebtClientImpl implements DebtClient {

    private static final Logger log = LoggerFactory.getLogger(DebtClientImpl.class);

    private final RestTemplate restTemplate;
    private final String debtServiceBaseUrl;

    public DebtClientImpl(
            RestTemplate restTemplate,
            @Value("${app.debt-service.base-url}") String debtServiceBaseUrl) {
        this.restTemplate      = restTemplate;
        this.debtServiceBaseUrl = debtServiceBaseUrl;
    }

    // ── Buscar deuda por ID ──────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Llama a {@code GET /api/v1/debts/{debtId}} en debt-service.
     * Si la deuda no existe (404), retorna {@link Optional#empty()}.
     * En caso de error de conectividad, el Circuit Breaker activa el fallback.
     */
    @Override
    @CircuitBreaker(name = "debtClient", fallbackMethod = "findByIdFallback")
    @Retry(name = "debtClient")
    public Optional<DebtDto> findById(String debtId) {
        String url = debtServiceBaseUrl + "/api/v1/debts/" + debtId;
        try {
            ResponseEntity<ApiResponse<DebtDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<DebtDto>>() {}
            );
            if (response.getBody() != null && response.getBody().data() != null) {
                return Optional.of(response.getBody().data());
            }
            return Optional.empty();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Deuda {} no encontrada en debt-service", debtId);
            return Optional.empty();
        }
    }

    // ── Buscar deudas activas por fecha de vencimiento ───────

    /**
     * {@inheritDoc}
     *
     * <p>Llama a {@code GET /api/v1/debts?dueDate=YYYY-MM-DD&status=ACTIVA}
     * en debt-service para obtener las deudas activas con esa fecha exacta.
     */
    @Override
    @CircuitBreaker(name = "debtClient", fallbackMethod = "findActiveDebtsFallback")
    @Retry(name = "debtClient")
    public List<DebtDto> findActiveDebtsWithDueDate(LocalDate dueDate) {
        String url = debtServiceBaseUrl
                + "/api/v1/debts?dueDate=" + dueDate
                + "&status=ACTIVA";
        try {
            ResponseEntity<ApiResponse<List<DebtDto>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<List<DebtDto>>>() {}
            );
            if (response.getBody() != null && response.getBody().data() != null) {
                return response.getBody().data();
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error al consultar deudas con vencimiento {}: {}", dueDate, e.getMessage());
            return Collections.emptyList();
        }
    }

    // ── Fallbacks del Circuit Breaker ────────────────────────

    @SuppressWarnings("unused")
    private Optional<DebtDto> findByIdFallback(String debtId, Throwable t) {
        log.warn("Circuit Breaker activo para findById({}): {}", debtId, t.getMessage());
        return Optional.empty();
    }

    @SuppressWarnings("unused")
    private List<DebtDto> findActiveDebtsFallback(LocalDate dueDate, Throwable t) {
        log.warn("Circuit Breaker activo para findActiveDebts({}): {}", dueDate, t.getMessage());
        return Collections.emptyList();
    }

    // ── Wrapper genérico para la respuesta de la API ─────────

    /**
     * Record interno que mapea el envoltorio estándar {@code ApiResponse}
     * que usan todos los microservicios del sistema.
     */
    private record ApiResponse<T>(
            String traceId,
            boolean success,
            T data,
            Object error
    ) {}
}
