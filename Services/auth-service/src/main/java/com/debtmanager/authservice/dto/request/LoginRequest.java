package com.debtmanager.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para el endpoint de login.
 */
@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

    /**
     * Correo del usuario.
     */
    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "El correo debe tener un formato válido.")
    private String email;

    /**
     * Contraseña del usuario.
     */
    @NotBlank(message = "La contraseña es obligatoria.")
    private String password;
}
