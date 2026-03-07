//! Qué hará esta clase
/* Tu JwtService tendrá estas responsabilidades:

generateToken(...)

extractEmail(...)

extractRole(...)

extractUserId(...)

isTokenValid(...)

isTokenExpired(...)
*/

package com.debtmanager.authservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio encargado de toda la lógica relacionada con JWT.
 *
 * Responsabilidades:
 * - Generar tokens
 * - Extraer información desde el token
 * - Validar token
 * - Verificar expiración
 *
 * Esta clase sigue el principio de responsabilidad única (SRP),
 * ya que solo se encarga de la gestión de JWT.
 */
@Service
public class JwtService {

    /**
     * Clave secreta usada para firmar y validar los tokens.
     * Se lee desde application.properties.
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Tiempo de expiración del token en milisegundos.
     * Se lee desde application.properties.
     */
    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Genera un token JWT con los datos mínimos requeridos por el estándar del
     * sistema.
     *
     * Claims incluidos:
     * - sub -> id del usuario
     * - email -> correo del usuario
     * - role -> rol del usuario
     *
     * @param userId id del usuario
     * @param email  correo del usuario
     * @param role   rol del usuario
     * @return token JWT firmado
     */
    public String generateToken(String userId, String email, String role) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("email", email);
        extraClaims.put("role", role);

        return buildToken(extraClaims, userId);
    }

    /**
     * Construye el token JWT con claims extra, subject y fecha de expiración.
     *
     * @param extraClaims claims personalizados
     * @param subject     normalmente el id del usuario
     * @return token JWT firmado
     */
    private String buildToken(Map<String, Object> extraClaims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Extrae el id del usuario desde el token.
     * En JWT, el id se almacena en el claim estándar "sub".
     *
     * @param token token JWT
     * @return id del usuario
     */
    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extrae el email desde el token.
     *
     * @param token token JWT
     * @return email del usuario
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    /**
     * Extrae el rol desde el token.
     *
     * @param token token JWT
     * @return rol del usuario
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Verifica si el token es válido.
     *
     * Un token es válido si:
     * - puede leerse correctamente
     * - no está expirado
     *
     * @param token token JWT
     * @return true si es válido, false si no
     */
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si el token ya expiró.
     *
     * @param token token JWT
     * @return true si expiró, false si todavía es válido
     */
    public boolean isTokenExpired(String token) {
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
     * Convierte la clave secreta configurada en application.properties
     * a una clave criptográfica válida para firmar/verificar JWT.
     *
     * Importante:
     * La clave debe estar en Base64 para que esta implementación funcione
     * correctamente.
     *
     * @return clave criptográfica
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Devuelve el tiempo de expiración configurado del JWT.
     *
     * @return tiempo de expiración en milisegundos
     */
    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }
}
