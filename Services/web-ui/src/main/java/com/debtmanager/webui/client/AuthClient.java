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

        Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
        return (String) data.get("token");
    }
}
