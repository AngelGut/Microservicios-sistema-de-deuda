package com.debtmanager.webui.service;

import com.debtmanager.webui.client.AuthClient;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthClient authClient;

    public AuthService(AuthClient authClient) {
        this.authClient = authClient;
    }

    public String login(String email, String password) {
        return authClient.login(email, password);
    }

    public void register(String fullName, String username, String email, String password) {
        authClient.register(fullName, username, email, password);
    }
}
