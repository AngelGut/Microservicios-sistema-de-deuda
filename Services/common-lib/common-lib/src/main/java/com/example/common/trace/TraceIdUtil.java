package com.example.common.trace;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * TraceIdUtil
 * -----------
 * Utilidad para manejar traceId/correlationId por request.
 *
 * Recomendación: usar MDC para que el traceId salga en logs automáticamente.
 */
public final class TraceIdUtil {

  public static final String TRACE_ID_KEY = "traceId"; // clave dentro de MDC
  public static final String TRACE_HEADER = "X-Trace-Id"; // header estándar

  private TraceIdUtil() {
    // Utility class: no instanciable
  }

  /**
   * Obtiene el traceId actual (desde MDC).
   * Si no existe, devuelve "N/A" para evitar nulls.
   */
  public static String getTraceId() {
    String traceId = MDC.get(TRACE_ID_KEY);
    return (traceId == null || traceId.isBlank()) ? "N/A" : traceId;
  }

  /**
   * Setea el traceId actual (en MDC).
   */
  public static void setTraceId(String traceId) {
    if (traceId == null || traceId.isBlank()) {
      traceId = generate();
    }
    MDC.put(TRACE_ID_KEY, traceId);
  }

  /**
   * Limpia el traceId del contexto (IMPORTANTE para no mezclar requests).
   */
  public static void clear() {
    MDC.remove(TRACE_ID_KEY);
  }

  /**
   * Genera un traceId nuevo.
   */
  public static String generate() {
    // UUID sin guiones (más compacto)
    return UUID.randomUUID().toString().replace("-", "");
  }
}
