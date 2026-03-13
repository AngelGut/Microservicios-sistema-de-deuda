package com.example.userservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

/**
 * DTO para recibir los datos al crear un nuevo usuario.
 * Las validaciones garantizan que los datos lleguen correctos.
 */
@Data
public class CreateUserRequest {

    /** Nombre de usuario único para iniciar sesión */
    @NotBlank(message = "El username es requerido")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    private String username;

    /** Email del usuario */
    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    /** Contraseña en texto plano — se encriptará antes de guardar */
    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    /** Nombre completo del usuario */
    @NotBlank(message = "El nombre completo es requerido")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String fullName;

    /** IDs de los roles a asignar al usuario */
    @NotEmpty(message = "Debe asignar al menos un rol al usuario")
    private Set<String> roleIds;
}
