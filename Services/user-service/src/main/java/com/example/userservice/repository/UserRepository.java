package com.example.userservice.repository;

import com.example.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad User.
 * Extiende JpaRepository para obtener operaciones CRUD gratuitas.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /** Busca un usuario por su username */
    Optional<User> findByUsername(String username);

    /** Busca un usuario por su email */
    Optional<User> findByEmail(String email);

    /** Verifica si ya existe un usuario con ese username */
    boolean existsByUsername(String username);

    /** Verifica si ya existe un usuario con ese email */
    boolean existsByEmail(String email);
}
