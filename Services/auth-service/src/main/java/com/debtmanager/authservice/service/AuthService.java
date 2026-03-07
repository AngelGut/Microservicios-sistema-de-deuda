package com.debtmanager.authservice.service;

import com.debtmanager.authservice.dto.request.LoginRequest;
import com.debtmanager.authservice.dto.response.LoginResponse;
import com.debtmanager.authservice.dto.response.TokenValidationResponse;

/**
 * Contrato del servicio de autenticación.
 */
public interface AuthService {

    /**
     * Ejecuta el login del usuario.
     *
     * @param request datos de login
     * @return respuesta con token
     */
    LoginResponse login(LoginRequest request);

    /**
     * Valida un token JWT.
     *
     * @param token token JWT
     * @return resultado de la validación
     */
    TokenValidationResponse validateToken(String token);
}
