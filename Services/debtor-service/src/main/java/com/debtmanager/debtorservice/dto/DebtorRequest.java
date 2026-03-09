package com.debtmanager.debtorservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DebtorRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 50) String document,
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank @Size(max = 20) String type
) {
}
