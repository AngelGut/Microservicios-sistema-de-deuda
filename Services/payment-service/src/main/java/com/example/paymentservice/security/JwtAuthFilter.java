package com.example.paymentservice.security;

import com.example.common.api.ApiErrorResponse;
import com.example.common.trace.TraceIdUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;

/**
 * Filtro de autenticación JWT.
 *
 * <p>
 * Principio SRP: su única responsabilidad es validar el token JWT
 * y poblar el SecurityContext de Spring Security.
 *
 * <p>
 * Implementa el estándar definido en system-standards.md:
 * <ul>
 * <li>Header: {@code Authorization: Bearer <token>}</li>
 * <li>Claims requeridos: sub, role, email/username, exp</li>
 * <li>401 si token inválido/expirado/ausente</li>
 * </ul>
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final SecretKey signingKey;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(@Value("${app.jwt.secret}") String secret) {
        SecretKey key;
        try {
            byte[] decodedKey = Decoders.BASE64.decode(secret);
            key = Keys.hmacShaKeyFor(decodedKey);
        } catch (IllegalArgumentException e) {
            key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
        this.signingKey = key;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // Sin token → continuar sin autenticar (Spring Security rechazará si el
        // endpoint lo exige)
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String sub = claims.getSubject();
            String role = claims.get("role", String.class);

            if (sub == null || role == null) {
                sendAuthError(response, "Token no contiene claims requeridos (sub, role)");
                return;
            }

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    sub, token, List.of(new SimpleGrantedAuthority("ROLE_" + role)));

            SecurityContextHolder.getContext().setAuthentication(auth);
            chain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.warn("JWT expirado: {}", e.getMessage());
            sendAuthError(response, "Token expirado");
        } catch (JwtException e) {
            log.warn("JWT inválido: {}", e.getMessage());
            sendAuthError(response, "Token inválido");
        }
    }

    // ── Helpers ──────────────────────────────────────────────

    private void sendAuthError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiErrorResponse body = ApiErrorResponse.of(
                "AUTH_401", message, TraceIdUtil.getTraceId());

        objectMapper.writeValue(response.getWriter(), body);
    }
}
