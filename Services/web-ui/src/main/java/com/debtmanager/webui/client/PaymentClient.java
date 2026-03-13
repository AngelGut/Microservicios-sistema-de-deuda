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
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // ── Lista global de todos los pagos ──────────────────────────────────────
    public List<PaymentResponse> getAll(String token) {
        try {
            HttpEntity<?> entity = new HttpEntity<>(buildHeaders(token));
            ResponseEntity<Map> response = restTemplate.exchange(
                    gatewayUrl + "/api/v1/payments",
                    HttpMethod.GET, entity, Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
                if (data != null)
                    return data.stream().map(this::mapToPaymentResponse).toList();
            }
        } catch (Exception e) {
            log.warn("[PaymentClient] getAll error: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    // ── Dashboard: últimos pagos (simulado desde getAll) ──────────────────────
    public List<RecentPaymentResponse> getRecent(int limit, String token) {
        try {
            List<PaymentResponse> all = getAll(token);
            return all.stream()
                    .limit(limit)
                    .map(p -> new RecentPaymentResponse(
                            p.id(), p.debtId(), null,
                            p.amount(), "DOP",
                            p.reference(), p.status(), p.paidAt()))
                    .toList();
        } catch (Exception e) {
            log.warn("[PaymentClient] getRecent no disponible: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    // ── Historial por deuda ───────────────────────────────────────────────────
    public List<PaymentResponse> getByDebtId(String debtId, String token) {
        try {
            HttpEntity<?> entity = new HttpEntity<>(buildHeaders(token));
            ResponseEntity<Map> response = restTemplate.exchange(
                    gatewayUrl + "/api/v1/payments/by-debt/" + debtId,
                    HttpMethod.GET, entity, Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
                if (data != null)
                    return data.stream().map(this::mapToPaymentResponse).toList();
            }
        } catch (Exception e) {
            log.warn("[PaymentClient] getByDebtId error: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    // ── Registrar pago — adapta web-ui request al DTO del payment-service ────
    public void create(PaymentRequest request, String token) {
        // web-ui envía: debtId (String UUID), amount, reference, notes
        // payment-service espera: debtId (String UUID), amount, paymentDate, note
        Map<String, Object> body = new HashMap<>();
        body.put("debtId", request.debtId());
        body.put("amount", request.amount());
        body.put("paymentDate", LocalDate.now().toString());
        // Usar reference o notes como "note"
        String note = request.notes() != null ? request.notes()
                : request.reference() != null ? request.reference() : "";
        body.put("note", note);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeaders(token));
        restTemplate.postForEntity(gatewayUrl + "/api/v1/payments", entity, Map.class);
        log.info("[PaymentClient] Pago registrado para deuda: {}", request.debtId());
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private PaymentResponse mapToPaymentResponse(Map<String, Object> m) {
        return new PaymentResponse(
                m.get("id") != null ? m.get("id").toString() : null,
                m.get("debtId") != null ? m.get("debtId").toString() : null,
                m.get("amount") != null ? new BigDecimal(m.get("amount").toString()) : BigDecimal.ZERO,
                m.get("reference") != null ? m.get("reference").toString() : null,
                m.get("note") != null ? m.get("note").toString() : null,
                "APPLIED",
                m.get("paymentDate") != null ? m.get("paymentDate").toString() : null);
    }
}
