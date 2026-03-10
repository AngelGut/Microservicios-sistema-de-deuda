package com.debtmanager.webui.client;

import com.debtmanager.webui.dto.request.DebtorRequest;
import com.debtmanager.webui.dto.response.DebtorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DebtorClient {

    private static final Logger log = LoggerFactory.getLogger(DebtorClient.class);

    private final RestTemplate restTemplate;
    private final String gatewayUrl;

    public DebtorClient(RestTemplate restTemplate,
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

    private DebtorResponse mapToResponse(Map<String, Object> m) {
        // debtor-service devuelve: id (Long), name, document, email, type
        // web-ui espera: id (String), name, type, documentType, documentNumber, email,
        // phone
        String id = m.get("id") != null ? m.get("id").toString() : null;
        String name = (String) m.get("name");
        String type = (String) m.get("type");
        String document = (String) m.get("document");
        String email = (String) m.get("email");
        return new DebtorResponse(id, name, type, null, document, email, null);
    }

    public List<DebtorResponse> getAll(String token) {
        HttpEntity<?> entity = new HttpEntity<>(buildHeaders(token));

        ResponseEntity<List> response = restTemplate.exchange(
                gatewayUrl + "/api/v1/debtors",
                HttpMethod.GET,
                entity,
                List.class);

        List<Map<String, Object>> body = response.getBody();
        if (body == null)
            return new ArrayList<>();

        return body.stream().map(this::mapToResponse).toList();
    }

    public DebtorResponse getById(String id, String token) {
        HttpEntity<?> entity = new HttpEntity<>(buildHeaders(token));

        ResponseEntity<Map> response = restTemplate.exchange(
                gatewayUrl + "/api/v1/debtors/" + id,
                HttpMethod.GET,
                entity,
                Map.class);

        return mapToResponse(response.getBody());
    }

    public void create(DebtorRequest request, String token) {
        // Mapear del modelo web-ui al modelo debtor-service
        // web-ui tiene: name, type, documentType, documentNumber, email, phone
        // debtor-service espera: name, document, email, type
        Map<String, String> body = new HashMap<>();
        body.put("name", request.name());
        body.put("type", request.type());
        body.put("email", request.email());
        // Combinar documentType + documentNumber como "document" (ej: "RNC-123456789")
        String document = request.documentNumber();
        if (request.documentType() != null && !request.documentType().isBlank()) {
            document = request.documentType() + "-" + request.documentNumber();
        }
        body.put("document", document);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, buildHeaders(token));
        restTemplate.postForEntity(
                gatewayUrl + "/api/v1/debtors",
                entity,
                Map.class);

        log.info("[DebtorClient] Deudor creado: {}", request.name());
    }
}
