package com.debtmanager.authservice.service.impl;

import com.debtmanager.authservice.domain.model.User;
import com.debtmanager.authservice.dto.request.LoginRequest;
import com.debtmanager.authservice.dto.response.LoginResponse;
import com.debtmanager.authservice.dto.response.TokenValidationResponse;
import com.debtmanager.authservice.exception.InvalidCredentialsException;
import com.debtmanager.authservice.repository.UserRepository;
import com.debtmanager.authservice.security.JwtService;
import com.debtmanager.authservice.security.JwtValidator;
import com.debtmanager.authservice.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementación de la lógica del microservicio de autenticación.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtValidator jwtValidator;

    public AuthServiceImpl(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtValidator jwtValidator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        User user = userRepository.findByEmailAndEnabledTrue(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas."));

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!passwordMatches) {
            throw new InvalidCredentialsException("Credenciales inválidas.");
        }

        String token = jwtService.generateToken(
                user.getId(),
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
