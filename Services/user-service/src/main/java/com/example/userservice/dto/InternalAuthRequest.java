package com.example.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InternalAuthRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
