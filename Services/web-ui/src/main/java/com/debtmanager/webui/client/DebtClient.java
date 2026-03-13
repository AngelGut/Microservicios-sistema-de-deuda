package com.debtmanager.webui.client;

import com.debtmanager.webui.dto.request.DebtRequest;
import com.debtmanager.webui.dto.response.DebtResponse;
import com.debtmanager.webui.dto.response.DebtSummaryResponse;
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
 * Consume debt-service a través del api-gateway.
 *
 * Endpoints utilizados:
 * GET /api/v1/debts → todas las deudas (lista global)
 * GET /api/v1/debts?debtorId={id} → deudas de un deudor
 * GET /api/v1/debts/{id} → detalle de una deuda
 * GET /api/v1/debts/summary → resumen para dashboard (KPIs)
 * POST /api/v1/debts → crear nueva deuda
 *
 * Todos usan el formato estándar: { success, timestamp, traceId, data }
 */
@Component
public class DebtClient {

    private static final Logger log = LoggerFactory.getLogger(DebtClient.class);

    private final RestTemplate restTemplate;
    private final String gatewayUrl;

    public DebtClient(RestTemplate restTemplate,
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

    // ── Dashboard KPIs ────────────────────────────────────────────────────────
    public DebtSummaryResponse getSummary(String token) {
        try {
            HttpEntity<?> entity = new HttpEntity<>(buildHeaders(token));

            ResponseEntity<Map> response = restTemplate.exchange(
                    gatewayUrl + "/api/v1/debts/summary",
                    HttpMethod.GET,
                    entity,
                    Map.class);

            if (response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null) {
                    return new DebtSummaryResponse(
                            toLong(data.get("totalActivas")),
                            toLong(data.get("totalPagadas")),
                            toBigDecimal(data.get("montoTotalActivo")),
                            toBigDecimal(data.get("montoTotalCobrado")));
                }
            }
        } catch (Exception e) {
            log.warn("[DebtClient] getSummary no disponible: {}", e.getMessage());
        }
        return new DebtSummaryResponse(0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    // ── Dashboard KPIs por moneda ──────────────────────────────────────────────
    public DebtSummaryResponse getSummaryByDOP(String token) {
        try {
            HttpEntity<?> entity = new HttpEntity<>(buildHeaders(token));

            ResponseEntity<Map> response = restTemplate.exchange(
                    gatewayUrl + "/api/v1/debts/summary?currency=DOP",
                    HttpMethod.GET,
                    entity,
                    Map.class);

            if (response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null) {
                    return new DebtSummaryResponse(
                            toLong(data.get("totalActivas")),
                            toLong(data.get("totalPagadas")),
                            toBigDecimal(data.get("montoTotalActivo")),
                            toBigDecimal(data.get("montoTotalCobrado")));
                }
            }
        } catch (Exception e) {
            log.warn("[DebtClient] getSummaryByDOP error: {}", e.getMessage());
        }
        return new DebtSummaryResponse(0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public DebtSummaryResponse getSummaryByUSD(String token) {
        try {
            HttpEntity<?> entity = new HttpEntity<>(buildHeaders(token));

            ResponseEntity<Map> response = restTemplate.exchange(
                    gatewayUrl + "/api/v1/debts/summary?currency=USD",
                    HttpMethod.GET,
                    entity,
                    Map.class);

            if (response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null) {
                    return new DebtSummaryResponse(
                            toLong(data.get("totalActivas")),
                            toLong(data.get("totalPagadas")),
                            toBigDecimal(data.get("montoTotalActivo")),
                            toBigDecimal(data.get("montoTotalCobrado")));
                }
            }
        } catch (Exception e) {
            log.warn("[DebtClient] getSummaryByUSD error: {}", e.getMessage());
        }
        return new DebtSummaryResponse(0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    // ── Lista global de todas las deudas ──────────────────────────────────────
    public List<DebtResponse> getAll(String token) {
        try {
            HttpEntity<?> entity = new HttpEntity<>(buildHeaders(token));

            ResponseEntity<Map> response = restTemplate.exchange(
                    gatewayUrl + "/api/v1/debts",
                    HttpMethod.GET,
                    entity,
                    Map.class);

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
            return data.stream().map(this::mapToDebtResponse).toList();
        } catch (Exception e) {
            log.warn("[DebtClient] getAll error: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // ── Deudas por deudor ─────────────────────────────────────────────────────
    public List<DebtResponse> getByDebtorId(String debtorId, String token) {
        try {
            HttpEntity<?> entity = new HttpEntity<>(buildHeaders(token));

            ResponseEntity<Map> response = restTemplate.exchange(
                    gatewayUrl + "/api/v1/debts?debtorId=" + debtorId,
                    HttpMethod.GET,
                    entity,
                    Map.class);

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
            return data.stream().map(this::mapToDebtResponse).toList();
        } catch (Exception e) {
            log.warn("[DebtClient] getByDebtorId error: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public DebtResponse getById(String id, String token) {
        HttpEntity<?> entity = new HttpEntity<>(buildHeaders(token));

        ResponseEntity<Map> response = restTemplate.exchange(
                gatewayUrl + "/api/v1/debts/" + id,
                HttpMethod.GET,
                entity,
                Map.class);

        Map<String, Object> m = (Map<String, Object>) response.getBody().get("data");
        return mapToDebtResponse(m);
    }

    public void create(DebtRequest request, String token) {
        HttpEntity<DebtRequest> entity = new HttpEntity<>(request, buildHeaders(token));
        restTemplate.postForEntity(
                gatewayUrl + "/api/v1/debts",
                entity,
                Map.class);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private DebtResponse mapToDebtResponse(Map<String, Object> m) {
        return new DebtResponse(
                (String) m.get("id"),
                (String) m.get("debtorId"),
                (String) m.get("description"),
                toBigDecimal(m.get("originalAmount")),
                toBigDecimal(m.get("currentBalance")),
                (String) m.get("currency"),
                (String) m.get("status"),
                (String) m.get("dueDate"));
    }

    private long toLong(Object val) {
        if (val == null)
            return 0L;
        return Long.parseLong(val.toString());
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null)
            return BigDecimal.ZERO;
        return new BigDecimal(val.toString());
    }
}
