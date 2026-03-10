package com.debtmanager.debtorservice.dto;

import jakarta.validation.constraints.Size;

public record DebtorRequest(
        @Size(max = 120) String name,
        @Size(max = 50) String document,
        @Size(max = 120) String email,
        @Size(max = 20) String type,
        @Size(max = 20) String phone) {
}
