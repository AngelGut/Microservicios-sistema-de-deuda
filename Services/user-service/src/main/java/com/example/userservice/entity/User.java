package com.example.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa un usuario del sistema.
 * Un usuario tiene credenciales de acceso y uno o más roles.
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    /** Nombre de usuario único para iniciar sesión */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** Email único del usuario */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /** Contraseña encriptada con BCrypt */
    @Column(nullable = false, length = 255)
    private String password;

    /** Nombre completo del usuario */
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    /** Si el usuario está activo o desactivado */
    @Column(nullable = false)
    private boolean active;

    /** Roles asignados al usuario */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
