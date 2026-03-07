package com.debtmanager.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de salida para validación de token.
 */
@Getter
@Setter
@AllArgsConstructor
public class TokenValidationResponse {

    /**
     * Indica si el token es válido.
     */
    private boolean valid;

    /**
     * Subject del token.
     * En este proyecto será el id del usuario.
     */
    private String subject;

    /**
     * Rol contenido dentro del token.
     */
    private String role;

    /**
     * Correo contenido dentro del token.
     */
    private String email;
}
