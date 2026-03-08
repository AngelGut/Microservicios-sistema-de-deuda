package com.debtmanager.webui.client;

import com.debtmanager.webui.dto.response.AiRiskResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Consume ai-risk-service a través del api-gateway.
 * Endpoint: GET /api/v1/ai/risk/debtor/{debtorId}
 *
 * Respuesta estándar:
 * { "data": { "status": "FIABLE|REVISION|BLOQUEADO", "explanation": "...",
 * "confidence": 0.9 } }
 *
 * Según la arquitectura (RNF-02):
 * "Un fallo en servicios no críticos (IA) no debe interrumpir operaciones
 * core."
 * Fallback: retorna AiRiskResponse("NO_DISPONIBLE", "Servicio no disponible",
 * null)
 */
@Component
public class AiRiskClient {

    private static final Logger log = LoggerFactory.getLogger(AiRiskClient.class);

    private static final AiRiskResponse FALLBACK = new AiRiskResponse("NO_DISPONIBLE",
            "Servicio de riesgo no disponible temporalmente", null);

    private final RestTemplate restTemplate;
    private final String gatewayUrl;

    public AiRiskClient(RestTemplate restTemplate,
            @Value("${gateway.base-url}") String gatewayUrl) {
        this.restTemplate = restTemplate;
        this.gatewayUrl = gatewayUrl;
    }

    public AiRiskResponse getRiskByDebtorId(String debtorId, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    gatewayUrl + "/api/v1/ai/risk/debtor/" + debtorId,
                    HttpMethod.GET,
                    entity,
                    Map.class);

            if (response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null) {
                    return new AiRiskResponse(
                            (String) data.get("status"),
                            (String) data.get("explanation"),
                            data.get("confidence") != null
                                    ? Double.parseDouble(data.get("confidence").toString())
                                    : null);
                }
            }
        } catch (Exception e) {
            log.warn("[AiRiskClient] ai-risk-service no disponible para deudor {}: {}",
                    debtorId, e.getMessage());
        }
        return FALLBACK;
    }
}
