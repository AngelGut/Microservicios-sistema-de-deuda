package com.debtmanager.authservice.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa a un usuario autenticable dentro del sistema.
 *
 * Tabla mínima para el MVP:
 * - id
 * - email
 * - password
 * - role
 * - enabled
 * - createdAt
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    /**
     * Identificador único del usuario.
     * Se usará UUID en formato String.
     */
    @Id
    @Column(nullable = false, length = 36)
    private String id;

    /**
     * Correo del usuario.
     * Debe ser único porque se usará para login.
     */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Contraseña del usuario.
     * Debe almacenarse en formato encriptado con BCrypt.
     */
    @Column(nullable = false, length = 255)
    private String password;

    /**
     * Rol del usuario.
     * Ejemplo: ADMIN, USER
     */
    @Column(nullable = false, length = 50)
    private String role;

    /**
     * Indica si el usuario está habilitado.
     */
    @Column(nullable = false)
    private boolean enabled;

    /**
     * Fecha de creación del registro.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Método ejecutado antes de insertar el registro por primera vez.
     * Aquí inicializamos valores automáticos.
     */
    @PrePersist
    public void prePersist() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }

        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
