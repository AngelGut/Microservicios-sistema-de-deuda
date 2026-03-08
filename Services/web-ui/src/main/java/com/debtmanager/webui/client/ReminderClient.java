package com.debtmanager.webui.client;

import com.debtmanager.webui.dto.response.ReminderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consume notification-service a través del api-gateway.
 *
 * Endpoints utilizados:
 * GET /api/v1/reminders?upcoming=true&days=30 → próximos recordatorios
 * POST /api/v1/reminders → crear nuevo recordatorio
 *
 * Formato estándar: { success, timestamp, traceId, data }
 */
@Component
public class ReminderClient {

    private static final Logger log = LoggerFactory.getLogger(ReminderClient.class);

    private final RestTemplate restTemplate;
    private final String gatewayUrl;

    public ReminderClient(RestTemplate restTemplate,
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

    // ── Próximos recordatorios ─────────────────────────────────────────────────
    public List<ReminderResponse> getUpcoming(int days, String token) {
        try {
            HttpEntity<?> entity = new HttpEntity<>(buildHeaders(token));

            ResponseEntity<Map> response = restTemplate.exchange(
                    gatewayUrl + "/api/v1/reminders?upcoming=true&days=" + days,
                    HttpMethod.GET,
                    entity,
                    Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.getBody().get("data");
                if (dataList != null) {
                    return dataList.stream().map(m -> new ReminderResponse(
                            (String) m.get("id"),
                            (String) m.get("debtId"),
                            (String) m.get("debtorName"),
                            (String) m.get("description"),
                            (String) m.get("dueDate"),
                            m.get("amount") != null ? m.get("amount").toString() : null,
                            (String) m.get("currency"))).toList();
                }
            }
        } catch (Exception e) {
            log.warn("[ReminderClient] notification-service no disponible: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    // ── Crear recordatorio ─────────────────────────────────────────────────────
    public void create(String debtId, String description, String dueDate,
            int daysBefore, String token) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("debtId", debtId);
            body.put("description", description);
            body.put("dueDate", dueDate);
            body.put("daysBefore", daysBefore);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeaders(token));
            restTemplate.postForEntity(
                    gatewayUrl + "/api/v1/reminders",
                    entity,
                    Map.class);
        } catch (Exception e) {
            log.warn("[ReminderClient] create error: {}", e.getMessage());
            throw new RuntimeException("No se pudo crear el recordatorio: " + e.getMessage());
        }
    }
}
