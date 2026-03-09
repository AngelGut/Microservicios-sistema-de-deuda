package com.debtmanager.authservice.service.impl;

import com.debtmanager.authservice.client.UserServiceAuthClient;
import com.debtmanager.authservice.client.dto.UserServiceAuthResponse;
import com.debtmanager.authservice.dto.request.LoginRequest;
import com.debtmanager.authservice.dto.response.LoginResponse;
import com.debtmanager.authservice.dto.response.TokenValidationResponse;
import com.debtmanager.authservice.exception.InvalidCredentialsException;
import com.debtmanager.authservice.security.JwtService;
import com.debtmanager.authservice.security.JwtValidator;
import com.debtmanager.authservice.service.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Implementación de la lógica del microservicio de autenticación.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserServiceAuthClient userServiceAuthClient;
    private final JwtService jwtService;
    private final JwtValidator jwtValidator;

    public AuthServiceImpl(UserServiceAuthClient userServiceAuthClient,
            JwtService jwtService,
            JwtValidator jwtValidator) {
        this.userServiceAuthClient = userServiceAuthClient;
        this.jwtService = jwtService;
        this.jwtValidator = jwtValidator;
    }

    /**
     * Realiza el proceso de login:
     * 1. busca usuario
     * 2. valida que esté habilitado
     * 3. compara contraseña
     * 4. genera JWT
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        UserServiceAuthResponse user;
        try {
            user = userServiceAuthClient.verifyCredentials(request.getEmail(), request.getPassword());
        } catch (HttpStatusCodeException ex) {
            throw new InvalidCredentialsException("Credenciales inválidas.");
        }
        if (user == null) {
            throw new InvalidCredentialsException("Credenciales inválidas.");
        }

        String token = jwtService.generateToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole());

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getJwtExpirationMs(),
                user.getRole(),
                user.getEmail());
    }

    /**
     * Valida un token ya emitido.
     */
    @Override
    public TokenValidationResponse validateToken(String token) {
        return jwtValidator.validate(token);
    }
}
