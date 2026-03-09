package com.example.common.api;

import java.time.Instant;
import java.util.Map;

/**
 * ApiErrorResponse
 * ----------------
 * Envelope estándar para errores.
 *
 * Ejemplo:
 * {
 * "success": false,
 * "timestamp": "2026-03-05T19:20:30.123Z",
 * "traceId": "a92bd3f2",
 * "error": {
 * "code": "VALIDATION_ERROR",
 * "message": "Revisa los campos requeridos",
 * "details": { "field": "email" }
 * }
 * }
 */
public record ApiErrorResponse(
    boolean success,
    Instant timestamp,
    String traceId,
    ErrorBody error) {

  public record ErrorBody(
      String code,
      String message,
      Map<String, Object> details) {
  }

  public static ApiErrorResponse of(String code, String message, String traceId, Map<String, Object> details) {
    Map<String, Object> safeDetails = (details == null) ? Map.of() : details;

    return new ApiErrorResponse(
        false,
        Instant.now(),
        safeTraceId(traceId),
        new ErrorBody(code, message, safeDetails));
  }

  public static ApiErrorResponse of(String code, String message, String traceId) {
    return of(code, message, traceId, Map.of());
  }

  private static String safeTraceId(String traceId) {
    return (traceId == null || traceId.isBlank()) ? "N/A" : traceId;
  }
}
