package com.example.common.api;

import java.time.Instant;

/**
 * ApiResponse<T>
 * -------------
 * Envelope estándar para respuestas exitosas.
 *
 * Ejemplo:
 * {
 * "success": true,
 * "timestamp": "2026-03-05T19:20:30.123Z",
 * "traceId": "a92bd3f2",
 * "data": { ... }
 * }
 */
public record ApiResponse<T>(
        boolean success,
        Instant timestamp,
        String traceId,
        T data) {

    /**
     * Respuesta OK con payload.
     */
    public static <T> ApiResponse<T> ok(T data, String traceId) {
        return new ApiResponse<>(true, Instant.now(), safeTraceId(traceId), data);
    }

    /**
     * Respuesta OK sin payload (data = null).
     */
    public static ApiResponse<Void> ok(String traceId) {
        return new ApiResponse<>(true, Instant.now(), safeTraceId(traceId), null);
    }

    /**
     * Evita nulls en traceId para que el frontend/logs no sufran.
     */
    private static String safeTraceId(String traceId) {
        return (traceId == null || traceId.isBlank()) ? "N/A" : traceId;
    }
}
