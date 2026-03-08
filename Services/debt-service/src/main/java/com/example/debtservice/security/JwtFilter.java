package com.example.debtservice.security;

import com.example.common.trace.TraceIdUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * Filtro JWT que intercepta cada request HTTP.
 * Valida el token antes de que llegue al controlador.
 * Extiende OncePerRequestFilter para ejecutarse una sola vez por request.
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    /** Servicio para validar y extraer datos del token */
    private final JwtService jwtService;

    private final ObjectMapper objectMapper;

    /**
     * Evita bloquear preflight CORS (OPTIONS), ya que no envía token.
     * Si se bloquea, el frontend ve errores JSON de autenticación antes del request real.
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return HttpMethod.OPTIONS.matches(request.getMethod());
    }

    /**
     * Intercepta cada request y valida el JWT.
     * Si el token es válido, deja pasar el request.
     * Si no, retorna 401 Unauthorized.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Obtenemos el header Authorization
        String authHeader = request.getHeader("Authorization");

        // Validamos que el header exista y tenga el formato correcto
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(response, "TOKEN_MISSING", "Token de autorización requerido");
            return;
        }

        // Extraemos el token removiendo el prefijo "Bearer "
        String token = authHeader.substring(7);

        // Validamos que el token sea válido
        if (!jwtService.isTokenValid(token)) {
            sendUnauthorized(response, "TOKEN_INVALID", "Token inválido o expirado");
            return;
        }

        // Token válido — seteamos el traceId para logs y respuestas
        TraceIdUtil.setTraceId(request.getHeader(TraceIdUtil.TRACE_HEADER));

        // Dejamos pasar el request al controlador
        filterChain.doFilter(request, response);

        // Limpiamos el traceId al finalizar el request
        TraceIdUtil.clear();
    }

    /**
     * Envía una respuesta 401 Unauthorized en formato estándar del equipo.
     *
     * @param response respuesta HTTP
     * @param code     código de error
     * @param message  mensaje de error
     */
    private void sendUnauthorized(HttpServletResponse response,
            String code,
            String message) throws IOException {

        // Construimos el envelope de error estándar manualmente
        Map<String, Object> error = Map.of(
                "success", false,
                "timestamp", Instant.now().toString(),
                "traceId", TraceIdUtil.getTraceId(),
                "error", Map.of(
                        "code", code,
                        "message", message,
                        "details", Map.of()));

        // Configuramos la respuesta como JSON con status 401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
