package com.example.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InternalAuthResponse {
    private String userId;
    private String email;
    private String role;
}
