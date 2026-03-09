package com.debtmanager.webui.client;

import com.debtmanager.webui.dto.request.PaymentRequest;
import com.debtmanager.webui.dto.response.PaymentResponse;
import com.debtmanager.webui.dto.response.RecentPaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Consume payment-service a través del api-gateway.
 *
 * Endpoints utilizados:
 * GET /api/v1/payments → todos los pagos (historial global)
 * GET /api/v1/payments?debtId={id} → historial de pagos de una deuda
 * GET /api/v1/payments/recent?limit=7 → últimos N pagos (para dashboard)
 * POST /api/v1/payments → registrar nuevo pago
 *
 * Todos usan el formato estándar: { success, timestamp, traceId, data }
 */
@Component
public class PaymentClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentClient.class);

    private final RestTemplate restTemplate;
    private final String gatewayUrl;

    public PaymentClient(RestTemplate restTemplate,
            @Value("${gateway.base-url}") String gatewayUrl) {
        this.restTemplate = restTemplate;
        this.gatewayUrl = gatewayUrl;
    }

    private HttpHeaders buildHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // ── Lista global de todos los pagos ───────────────────────────────────────
    public List<PaymentResponse> getAll(String token) {
        try {
            HttpEntity<?> entity = new HttpEntity<>(buildHeaders(token));

            ResponseEntity<Map> response = restTemplate.exchange(
                    gatewayUrl + "/api/v1/payments",
                    HttpMethod.GET,
                    entity,
                    Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
                if (data != null) {
                    return data.stream().map(this::mapToPaymentResponse).toList();
                }
            }
        } catch (Exception e) {
            log.warn("[PaymentClient] getAll error: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    // ── Dashboard: actividad reciente ─────────────────────────────────────────
    public List<RecentPaymentResponse> getRecent(int limit, String token) {
        try {
            HttpEntity<?> entity = new HttpEntity<>(buildHeaders(token));

            ResponseEntity<Map> response = restTemplate.exchange(
                    gatewayUrl + "/api/v1/payments/recent?limit=" + limit,
                    HttpMethod.GET,
                    entity,
                    Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.getBody().get("data");
                if (dataList != null) {
                    return dataList.stream().map(m -> new RecentPaymentResponse(
                            (String) m.get("id"),
                            (String) m.get("debtId"),
                            (String) m.get("debtorName"),
                            m.get("amount") != null ? new BigDecimal(m.get("amount").toString()) : BigDecimal.ZERO,
                            (String) m.get("currency"),
                            (String) m.get("reference"),
                            (String) m.get("status"),
                            (String) m.get("paidAt"))).toList();
                }
            }
        } catch (Exception e) {
            log.warn("[PaymentClient] getRecent no disponible: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    // ── Historial de pagos por deuda ──────────────────────────────────────────
    public List<PaymentResponse> getByDebtId(String debtId, String token) {
        try {
            HttpEntity<?> entity = new HttpEntity<>(buildHeaders(token));

            ResponseEntity<Map> response = restTemplate.exchange(
                    gatewayUrl + "/api/v1/payments?debtId=" + debtId,
                    HttpMethod.GET,
                    entity,
                    Map.class);

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
            return data.stream().map(this::mapToPaymentResponse).toList();
        } catch (Exception e) {
            log.warn("[PaymentClient] getByDebtId error: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // ── Registrar pago ────────────────────────────────────────────────────────
    public void create(PaymentRequest request, String token) {
        HttpEntity<PaymentRequest> entity = new HttpEntity<>(request, buildHeaders(token));
        restTemplate.postForEntity(
                gatewayUrl + "/api/v1/payments",
                entity,
                Map.class);
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private PaymentResponse mapToPaymentResponse(Map<String, Object> m) {
        return new PaymentResponse(
                (String) m.get("id"),
                (String) m.get("debtId"),
                m.get("amount") != null ? new BigDecimal(m.get("amount").toString()) : BigDecimal.ZERO,
                (String) m.get("reference"),
                (String) m.get("notes"),
                (String) m.get("status"),
                (String) m.get("paidAt"));
    }
}
