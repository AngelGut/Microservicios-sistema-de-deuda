package com.debtmanager.authservice.controller;

import com.debtmanager.authservice.dto.request.LoginRequest;
import com.debtmanager.authservice.dto.response.LoginResponse;
import com.debtmanager.authservice.dto.response.TokenValidationResponse;
import com.debtmanager.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de autenticación.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint de login.
     *
     * @param request datos de autenticación
     * @return token JWT si las credenciales son válidas
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Endpoint para validar token.
     *
     * El token se recibe en el header:
     * Authorization: Bearer <token>
     *
     * @param authorizationHeader header Authorization
     * @return resultado de la validación
     */
    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validate(
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.replace("Bearer ", "").trim();
        return ResponseEntity.ok(authService.validateToken(token));
    }
}
