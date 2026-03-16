package com.debtmanager.authservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserServiceAuthRequest {
    private String email;
    private String password;
}
