package com.debtmanager.webui.dto.request;

public record DebtorRequest(
        String name,
        String type,
        String documentType,
        String documentNumber,
        String email,
        String phone) {
}
