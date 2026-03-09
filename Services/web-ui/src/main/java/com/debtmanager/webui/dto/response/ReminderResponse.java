package com.debtmanager.webui.dto.response;

/**
 * Próximos vencimientos / recordatorios.
 * GET /api/v1/reminders?upcoming=true&days=30
 *
 * Endpoint que debe exponer notification-service.
 */
public record ReminderResponse(
        String id,
        String debtId,
        String debtorName,
        String description,
        String dueDate, // ISO date: "2026-03-31"
        String amount, // monto formateado (puede ser null)
        String currency) {
}
