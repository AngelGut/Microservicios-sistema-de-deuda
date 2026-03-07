package com.debtmanager.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de salida para un login exitoso.
 */
@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {

    /**
     * Token JWT generado.
     */
    private String token;

    /**
     * Tipo de token.
     * Normalmente será Bearer.
     */
    private String tokenType;

    /**
     * Tiempo de expiración en milisegundos.
     */
    private long expiresIn;

    /**
     * Rol del usuario autenticado.
     */
    private String role;

    /**
     * Correo del usuario autenticado.
     */
    private String email;
}
