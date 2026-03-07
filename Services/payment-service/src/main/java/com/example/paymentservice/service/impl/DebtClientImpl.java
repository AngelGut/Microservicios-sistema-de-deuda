package com.example.paymentservice.service.impl;

import com.example.paymentservice.dto.response.DebtResponse;
import com.example.paymentservice.service.DebtClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Implementación del cliente HTTP hacia debt-service.
 *
 * <p>Principio SRP: su única responsabilidad es comunicarse con debt-service.
 * Principio OCP: se puede extender (p.ej. añadir cache) sin modificar el contrato.
 *
 * <p>Resilience4j garantiza:
 * <ul>
 *   <li>{@code @CircuitBreaker} – abre el circuito si debt-service falla repetidamente</li>
 *   <li>{@code @Retry} – reintenta hasta 3 veces antes de abrir el circuito</li>
 * </ul>
 */
@Service
public class DebtClientImpl implements DebtClient {

    private static final Logger log = LoggerFactory.getLogger(DebtClientImpl.class);

    private final RestTemplate restTemplate;
    private final String debtServiceBaseUrl;

    public DebtClientImpl(
            RestTemplate restTemplate,
            @Value("${app.debt-service.base-url}") String debtServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.debtServiceBaseUrl = debtServiceBaseUrl;
    }

    /**
     * Consulta una deuda en debt-service.
     *
     * <p>Fallback: si debt-service no responde, devuelve {@code Optional.empty()}
     * para que el servicio de negocio maneje el caso adecuadamente.
     */
    @Override
    @CircuitBreaker(name = "debtClient", fallbackMethod = "findByIdFallback")
    @Retry(name = "debtClient")
    public Optional<DebtResponse> findById(Long debtId) {
        String url = debtServiceBaseUrl + "/debts/{id}";
        log.debug("Consultando debt-service: GET {}", url.replace("{id}", debtId.toString()));

        try {
            ResponseEntity<DebtServiceApiResponse> response =
                    restTemplate.getForEntity(url, DebtServiceApiResponse.class, debtId);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.ofNullable(response.getBody().data());
            }
            return Optional.empty();

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Deuda {} no encontrada en debt-service", debtId);
            return Optional.empty();
        }
    }

    /**
     * Notifica a debt-service que se registró un pago.
     */
    @Override
    @CircuitBreaker(name = "debtClient", fallbackMethod = "notifyPaymentFallback")
    @Retry(name = "debtClient")
    public void notifyPayment(Long debtId, BigDecimal paidAmount) {
        String url = debtServiceBaseUrl + "/debts/{id}/apply-payment";
        log.debug("Notificando pago a debt-service: POST {} amount={}", debtId, paidAmount);

        restTemplate.postForEntity(
                url,
                new ApplyPaymentRequest(paidAmount),
                Void.class,
                debtId
        );
    }

    // ── Fallbacks ────────────────────────────────────────────

    @SuppressWarnings("unused")
    private Optional<DebtResponse> findByIdFallback(Long debtId, Throwable ex) {
        log.error("Circuit breaker activo: no se pudo consultar deuda {} – {}", debtId, ex.getMessage());
        return Optional.empty();
    }

    @SuppressWarnings("unused")
    private void notifyPaymentFallback(Long debtId, BigDecimal paidAmount, Throwable ex) {
        log.error("Circuit breaker activo: no se pudo notificar pago para deuda {} – {}",
                debtId, ex.getMessage());
        // El pago ya fue registrado; se puede implementar un mecanismo de reintento
        // asíncrono/outbox para garantizar consistencia eventual.
    }

    // ── Inner records para mapeo de respuestas ────────────────

    /**
     * Estructura genérica de la ApiResponse de debt-service.
     * Solo se mapea el campo {@code data} que necesitamos.
     */
    private record DebtServiceApiResponse(boolean success, DebtResponse data) {}

    /** Request body para el endpoint apply-payment. */
    private record ApplyPaymentRequest(BigDecimal amount) {}
}
