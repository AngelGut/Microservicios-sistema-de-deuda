package com.example.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un rol en el sistema.
 * Un rol define los permisos y accesos que tiene un usuario.
 */
@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    /** Nombre único del rol (ej: ADMIN, OPERATOR, VIEWER) */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /** Descripción del rol */
    @Column(length = 200)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
