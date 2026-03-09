package com.example.common.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.Objects;

public class ApiException extends RuntimeException {

  private final String code;
  private final HttpStatus status;
  private final Map<String, Object> details;

  public ApiException(String code, String message, HttpStatus status, Map<String, Object> details) {
    super(message);
    this.code = Objects.requireNonNull(code, "code no puede ser null");
    this.status = Objects.requireNonNull(status, "status no puede ser null");

    Map<String, Object> safe = (details == null) ? Map.of() : details;
    this.details = safe.isEmpty() ? Map.of() : Map.copyOf(safe); // inmutable
  }

  // (Opcional) constructor con causa
  public ApiException(String code, String message, HttpStatus status, Map<String, Object> details, Throwable cause) {
    super(message, cause);
    this.code = Objects.requireNonNull(code, "code no puede ser null");
    this.status = Objects.requireNonNull(status, "status no puede ser null");

    Map<String, Object> safe = (details == null) ? Map.of() : details;
    this.details = safe.isEmpty() ? Map.of() : Map.copyOf(safe);
  }

  public String getCode() {
    return code;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public Map<String, Object> getDetails() {
    return details;
  }

  // Helpers

  public static ApiException badRequest(String code, String message) {
    return new ApiException(code, message, HttpStatus.BAD_REQUEST, Map.of());
  }

  public static ApiException badRequest(String code, String message, Map<String, Object> details) {
    return new ApiException(code, message, HttpStatus.BAD_REQUEST, details);
  }

  public static ApiException notFound(String code, String message) {
    return new ApiException(code, message, HttpStatus.NOT_FOUND, Map.of());
  }

  public static ApiException notFound(String code, String message, Map<String, Object> details) {
    return new ApiException(code, message, HttpStatus.NOT_FOUND, details);
  }

  public static ApiException unauthorized(String code, String message) {
    return new ApiException(code, message, HttpStatus.UNAUTHORIZED, Map.of());
  }

  public static ApiException forbidden(String code, String message) {
    return new ApiException(code, message, HttpStatus.FORBIDDEN, Map.of());
  }

  public static ApiException internal(String code, String message) {
    return new ApiException(code, message, HttpStatus.INTERNAL_SERVER_ERROR, Map.of());
  }
}
