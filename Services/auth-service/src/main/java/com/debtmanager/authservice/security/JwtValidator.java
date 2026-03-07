package com.debtmanager.authservice.security;

import com.debtmanager.authservice.dto.response.TokenValidationResponse;
import org.springframework.stereotype.Component;

/**
 * Clase de apoyo para validar token y construir una respuesta de validación.
 */
@Component
public class JwtValidator {

    private final JwtService jwtService;

    public JwtValidator(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Valida un token y construye la respuesta.
     *
     * @param token token JWT
     * @return respuesta de validación
     */
    public TokenValidationResponse validate(String token) {

        boolean valid = jwtService.isTokenValid(token);

        if (!valid) {
            return new TokenValidationResponse(
                    false,
                    null,
                    null,
                    null);
        }

        return new TokenValidationResponse(
                true,
                jwtService.extractUserId(token),
                jwtService.extractRole(token),
                jwtService.extractEmail(token));
    }
}
