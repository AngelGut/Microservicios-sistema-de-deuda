package com.example.userservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

/**
 * DTO para recibir los datos al actualizar un usuario existente.
 * Todos los campos son opcionales — solo se actualiza lo que llegue.
 */
@Data
public class UpdateUserRequest {

    /** Nuevo nombre completo del usuario */
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String fullName;

    /** Nueva contraseña — se encriptará antes de guardar */
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    /** Nuevos roles a asignar al usuario */
    private Set<String> roleIds;

    /** Activar o desactivar el usuario */
    private Boolean active;
}
