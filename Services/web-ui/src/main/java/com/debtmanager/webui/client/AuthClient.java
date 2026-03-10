package com.debtmanager.webui.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class AuthClient {

    private final RestTemplate restTemplate;
    private final String gatewayUrl;

    public AuthClient(RestTemplate restTemplate,
            @Value("${gateway.base-url}") String gatewayUrl) {
        this.restTemplate = restTemplate;
        this.gatewayUrl = gatewayUrl;
    }

    // ── Login ──────────────────────────────────────────────────────────────────
    public String login(String email, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "email", email,
                "password", password);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                gatewayUrl + "/api/v1/auth/login",
                request,
                Map.class);

        Map<?, ?> bodyData = response.getBody();
        if (bodyData == null) {
            throw new IllegalStateException("Respuesta vacía en login");
        }

        Object wrappedData = bodyData.get("data");
        if (wrappedData instanceof Map<?, ?> data && data.get("token") != null) {
            return data.get("token").toString();
        }

        if (bodyData.get("token") != null) {
            return bodyData.get("token").toString();
        }

        throw new IllegalStateException("No se recibió token de autenticación");
    }

    // ── Registro ───────────────────────────────────────────────────────────────
    // Endpoint: POST /api/v1/users (user-service vía gateway)
    // Body: { fullName, username, email, password, roleIds }
    public void register(String fullName, String username, String email, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "fullName", fullName,
                "username", username,
                "email", email,
                "password", password,
                "roleIds", java.util.List.of("2"));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        restTemplate.postForEntity(
                gatewayUrl + "/api/v1/users",
                request,
                Map.class);
    }
}
