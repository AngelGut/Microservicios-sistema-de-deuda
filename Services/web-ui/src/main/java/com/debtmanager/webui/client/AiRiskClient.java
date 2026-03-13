package com.debtmanager.webui.client;

import com.debtmanager.webui.dto.response.AiRiskResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class AiRiskClient {

    private static final Logger log = LoggerFactory.getLogger(AiRiskClient.class);
    private static final AiRiskResponse FALLBACK = new AiRiskResponse(
            "NO_DISPONIBLE", "Servicio de riesgo no disponible", null,
            null, null, null, null, null);

    private final RestTemplate restTemplate;
    private final String gatewayUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

            ResponseEntity<String> response = restTemplate.exchange(
                    gatewayUrl + "/api/v1/risk/" + debtorId,
                    HttpMethod.GET,
                    entity,
                    String.class);

            if (response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode data = root.path("data");

                String riskLevel = data.path("riskLevel").asText("NO_DISPONIBLE");
                String status = switch (riskLevel) {
                    case "GOOD_CLIENT" -> "FIABLE";
                    case "LOW_RISK" -> "REVISION";
                    case "HIGH_RISK" -> "BLOQUEADO";
                    default -> "NO_DISPONIBLE";
                };

                String explanation = "Score: "
                        + String.format("%.0f", data.path("riskScore").asDouble(0))
                        + " | Días mora: " + data.path("totalDaysLate").asInt(0);

                Double confidence = data.path("aiScore").isNull() ? null
                        : data.path("aiScore").asDouble() / 100.0;

                List<String> recs = new ArrayList<>();
                JsonNode recsNode = data.path("aiRecommendations");
                if (recsNode.isArray()) {
                    recsNode.forEach(n -> recs.add(n.asText()));
                }

                return new AiRiskResponse(
                        status,
                        explanation,
                        confidence,
                        data.path("riskScore").asDouble(0.0),
                        data.path("totalDaysLate").asInt(0),
                        data.path("latePaymentCount").asInt(0),
                        data.path("paymentCount").asInt(0),
                        recs);
            }
        } catch (Exception e) {
            log.warn("[AiRiskClient] ai-risk-service no disponible para deudor {}: {}",
                    debtorId, e.getMessage());
        }
        return FALLBACK;
    }

    public AiRiskResponse recalculate(String debtorId, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    gatewayUrl + "/api/v1/risk/recalculate/" + debtorId,
                    HttpMethod.POST,
                    entity,
                    String.class);

            if (response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode data = root.path("data");

                String riskLevel = data.path("riskLevel").asText("NO_DISPONIBLE");
                String status = switch (riskLevel) {
                    case "GOOD_CLIENT" -> "FIABLE";
                    case "LOW_RISK" -> "REVISION";
                    case "HIGH_RISK" -> "BLOQUEADO";
                    default -> "NO_DISPONIBLE";
                };

                String explanation = "Score: "
                        + String.format("%.0f", data.path("riskScore").asDouble(0))
                        + " | Días mora: " + data.path("totalDaysLate").asInt(0);

                Double confidence = data.path("aiScore").isNull() ? null
                        : data.path("aiScore").asDouble() / 100.0;

                List<String> recs = new ArrayList<>();
                JsonNode recsNode = data.path("aiRecommendations");
                if (recsNode.isArray()) {
                    recsNode.forEach(n -> recs.add(n.asText()));
                }

                return new AiRiskResponse(
                        status, explanation, confidence,
                        data.path("riskScore").asDouble(0.0),
                        data.path("totalDaysLate").asInt(0),
                        data.path("latePaymentCount").asInt(0),
                        data.path("paymentCount").asInt(0),
                        recs);
            }
        } catch (Exception e) {
            log.warn("[AiRiskClient] recalculate no disponible para deudor {}: {}",
                    debtorId, e.getMessage());
        }
        return FALLBACK;
    }
}
