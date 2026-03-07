package com.example.debtservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Servicio encargado de validar y extraer información de tokens JWT.
 * Usa la misma clave secreta que el auth-service para verificar los tokens.
 * Sigue el mismo patrón que JwtService del auth-service.
 */
@Service
public class JwtService {

    /**
     * Clave secreta leída desde application.properties.
     * Debe ser la misma que usa el auth-service para firmar los tokens.
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Extrae el ID del usuario desde el token.
     * En JWT el ID se guarda en el claim estándar "sub" (subject).
     *
     * @param token token JWT
     * @return ID del usuario
     */
    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extrae el email del usuario desde el token.
     *
     * @param token token JWT
     * @return email del usuario
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    /**
     * Extrae el rol del usuario desde el token.
     *
     * @param token token JWT
     * @return rol del usuario (ej: ADMIN, USER, SERVICE)
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Verifica si el token es válido.
     * Un token es válido si puede leerse correctamente y no está expirado.
     *
     * @param token token JWT
     * @return true si es válido, false si no
     */
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            // Si hay cualquier error al parsear el token, es inválido
            return false;
        }
    }

    /**
     * Verifica si el token ya expiró.
     *
     * @param token token JWT
     * @return true si expiró, false si todavía es válido
     */
    private boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    /**
     * Extrae todos los claims del token.
     *
     * @param token token JWT
     * @return claims del token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Convierte la clave secreta de Base64 a una clave criptográfica.
     *
     * @return clave criptográfica para verificar el token
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
