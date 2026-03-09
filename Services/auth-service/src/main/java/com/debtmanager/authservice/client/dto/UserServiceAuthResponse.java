package com.debtmanager.authservice.client.dto;

import lombok.Data;

@Data
public class UserServiceAuthResponse {
    private String userId;
    private String email;
    private String role;
}
