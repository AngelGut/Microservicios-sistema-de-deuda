package com.example.userservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO para devolver la información de un usuario al cliente.
 * Nunca incluye la contraseña por seguridad.
 */
@Data
@Builder
public class UserResponse {

    private String id;
    private String username;
    private String email;
    private String fullName;
    private boolean active;

    /** Nombres de los roles asignados al usuario */
    private Set<String> roles;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
