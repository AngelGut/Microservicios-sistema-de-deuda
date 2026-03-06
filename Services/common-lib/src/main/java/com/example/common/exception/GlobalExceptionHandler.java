package com.example.common.exception;

import com.example.common.api.ApiErrorResponse;
import com.example.common.trace.TraceIdUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex, HttpServletRequest req) {
        String traceId = TraceIdUtil.getTraceId();

        Map<String, Object> details = new HashMap<>(ex.getDetails());
        details.put("path", req.getRequestURI());

        ApiErrorResponse body = ApiErrorResponse.of(ex.getCode(), ex.getMessage(), traceId, details);
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
            HttpServletRequest req) {
        String traceId = TraceIdUtil.getTraceId();

        Map<String, Object> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> details.put(err.getField(), err.getDefaultMessage()));
        details.put("path", req.getRequestURI());

        ApiErrorResponse body = ApiErrorResponse.of(
                "VALIDATION_ERROR",
                "Revisa los campos requeridos",
                traceId,
                details);
        return ResponseEntity.badRequest().body(body);
    }

    // JSON mal formado / body que no se puede parsear
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleBadJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
        String traceId = TraceIdUtil.getTraceId();

        Map<String, Object> details = Map.of("path", req.getRequestURI());
        ApiErrorResponse body = ApiErrorResponse.of(
                "BAD_JSON",
                "El cuerpo (JSON) no es válido",
                traceId,
                details);
        return ResponseEntity.badRequest().body(body);
    }

    // Param/PathVariable con tipo incorrecto (ej: /debts/abc cuando esperas Long)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
            HttpServletRequest req) {
        String traceId = TraceIdUtil.getTraceId();

        Map<String, Object> details = new HashMap<>();
        details.put("param", ex.getName());
        details.put("value", ex.getValue());
        details.put("expectedType", ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        details.put("path", req.getRequestURI());

        ApiErrorResponse body = ApiErrorResponse.of(
                "TYPE_MISMATCH",
                "Parámetro inválido",
                traceId,
                details);
        return ResponseEntity.badRequest().body(body);
    }

    // Método HTTP no permitido (GET/POST/etc)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
            HttpServletRequest req) {
        String traceId = TraceIdUtil.getTraceId();

        Map<String, Object> details = Map.of(
                "path", req.getRequestURI(),
                "method", req.getMethod());

        ApiErrorResponse body = ApiErrorResponse.of(
                "METHOD_NOT_ALLOWED",
                "Método HTTP no permitido para este endpoint",
                traceId,
                details);
        return ResponseEntity.status(405).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAny(Exception ex, HttpServletRequest req) {
        String traceId = TraceIdUtil.getTraceId();

        // Log en servidor con traceId para depurar
        log.error("[traceId={}] Error no controlado en {} {}", traceId, req.getMethod(), req.getRequestURI(), ex);

        Map<String, Object> details = Map.of("path", req.getRequestURI());

        ApiErrorResponse body = ApiErrorResponse.of(
                "INTERNAL_ERROR",
                "Ocurrió un error inesperado",
                traceId,
                details);
        return ResponseEntity.internalServerError().body(body);
    }
}
