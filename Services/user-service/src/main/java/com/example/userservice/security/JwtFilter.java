package com.example.userservice.security;

import com.example.common.trace.TraceIdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Rutas públicas — sin token requerido
        // Se aceptan tanto /api/users como /api/v1/users porque el gateway
        // puede reenviar el path completo sin StripPrefix
        if (("POST".equalsIgnoreCase(method) && "/api/users".equals(path))
                || ("POST".equalsIgnoreCase(method) && "/api/v1/users".equals(path))
                || ("POST".equalsIgnoreCase(method) && "/api/users/login".equals(path))
                || ("POST".equalsIgnoreCase(method) && "/api/users/internal/verify-credentials".equals(path))
                || ("POST".equalsIgnoreCase(method) && "/api/v1/users/internal/verify-credentials".equals(path))) {
            filterChain.doFilter(request, response);
            return;
        }

        String traceId = request.getHeader(TraceIdUtil.TRACE_HEADER);
        if (traceId != null) {
            TraceIdUtil.setTraceId(traceId);
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Token requerido\"}");
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Token inválido\"}");
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TraceIdUtil.clear();
        }
    }
}
