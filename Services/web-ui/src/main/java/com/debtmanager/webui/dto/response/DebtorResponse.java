package com.debtmanager.webui.dto.response;

public record DebtorResponse(
        String id,
        String name,
        String type,
        String documentType,
        String documentNumber,
        String email,
        String phone) {
}
