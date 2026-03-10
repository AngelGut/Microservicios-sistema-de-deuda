package com.debtmanager.webui.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Consume fx-service a través del api-gateway.
 * Endpoint: GET /api/v1/fx/rate?from=USD&to=DOP
 *
 * Respuesta estándar (system-standards.md):
 * { "success": true, "data": { "rate": 59.84 } }
 *
 * Fallback: retorna null si el servicio no está disponible.
 * El dashboard muestra "N/D" sin interrumpir la pantalla.
 */
@Component
public class FxClient {

    private static final Logger log = LoggerFactory.getLogger(FxClient.class);

    private final RestTemplate restTemplate;
    private final String gatewayUrl;

    public FxClient(RestTemplate restTemplate,
            @Value("${gateway.base-url}") String gatewayUrl) {
        this.restTemplate = restTemplate;
        this.gatewayUrl = gatewayUrl;
    }

    public BigDecimal getUsdToDop(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    gatewayUrl + "/api/v1/fx/rate?from=USD&to=DOP",
                    HttpMethod.GET,
                    entity,
                    Map.class);

            if (response.getBody() != null) {
                // fx-service devuelve ConversionResponse directamente:
                // {from,to,amount,converted,rate}
                Object rate = response.getBody().get("rate");
                if (rate != null) {
                    return new BigDecimal(rate.toString());
                }
            }
        } catch (Exception e) {
            log.warn("[FxClient] fx-service no disponible: {}", e.getMessage());
        }
        return null; // fallback — dashboard muestra "N/D"
    }
}
