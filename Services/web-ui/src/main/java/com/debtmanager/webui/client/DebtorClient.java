package com.debtmanager.webui.client;

import com.debtmanager.webui.dto.request.DebtorRequest;
import com.debtmanager.webui.dto.response.DebtorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class DebtorClient {

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

    public List<DebtorResponse> getAll(String token) {
        HttpEntity<?> entity = new HttpEntity<>(buildHeaders(token));

        ResponseEntity<Map> response = restTemplate.exchange(
                gatewayUrl + "/api/v1/debtors",
                HttpMethod.GET,
                entity,
                Map.class);

        List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");

        return data.stream().map(m -> new DebtorResponse(
                (String) m.get("id"),
                (String) m.get("name"),
                (String) m.get("type"),
                (String) m.get("documentType"),
                (String) m.get("documentNumber"),
                (String) m.get("email"),
                (String) m.get("phone"))).toList();
    }

    public DebtorResponse getById(String id, String token) {
        HttpEntity<?> entity = new HttpEntity<>(buildHeaders(token));

        ResponseEntity<Map> response = restTemplate.exchange(
                gatewayUrl + "/api/v1/debtors/" + id,
                HttpMethod.GET,
                entity,
                Map.class);

        Map<String, Object> m = (Map<String, Object>) response.getBody().get("data");

        return new DebtorResponse(
                (String) m.get("id"),
                (String) m.get("name"),
                (String) m.get("type"),
                (String) m.get("documentType"),
                (String) m.get("documentNumber"),
                (String) m.get("email"),
                (String) m.get("phone"));
    }

    public void create(DebtorRequest request, String token) {
        HttpEntity<DebtorRequest> entity = new HttpEntity<>(request, buildHeaders(token));
        restTemplate.postForEntity(
                gatewayUrl + "/api/v1/debtors",
                entity,
                Map.class);
    }
}
