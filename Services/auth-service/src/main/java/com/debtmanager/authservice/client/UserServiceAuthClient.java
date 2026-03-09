package com.debtmanager.authservice.client;

import com.debtmanager.authservice.client.dto.UserServiceAuthRequest;
import com.debtmanager.authservice.client.dto.UserServiceAuthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceAuthClient {

    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;
    private final String internalKey;

    public UserServiceAuthClient(RestTemplate restTemplate,
            @Value("${user-service.base-url}") String userServiceBaseUrl,
            @Value("${auth.internal-key}") String internalKey) {
        this.restTemplate = restTemplate;
        this.userServiceBaseUrl = userServiceBaseUrl;
        this.internalKey = internalKey;
    }

    public UserServiceAuthResponse verifyCredentials(String email, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Key", internalKey);

        HttpEntity<UserServiceAuthRequest> entity = new HttpEntity<>(
                new UserServiceAuthRequest(email, password),
                headers);

        ResponseEntity<UserServiceAuthResponse> response = restTemplate.postForEntity(
                userServiceBaseUrl + "/api/users/internal/verify-credentials",
                entity,
                UserServiceAuthResponse.class);

        return response.getBody();
    }
}
