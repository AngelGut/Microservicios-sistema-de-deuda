package com.debtmanager.authservice.security;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase encargada de construir los claims personalizados del JWT.
 *
 * Tener esta responsabilidad en una clase separada ayuda a respetar SRP.
 */
@Component
public class JwtClaimsFactory {

    /**
     * Construye los claims personalizados del token.
     *
     * @param email correo del usuario
     * @param role  rol del usuario
     * @return mapa de claims
     */
    public Map<String, Object> buildClaims(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", role);
        return claims;
    }
}
