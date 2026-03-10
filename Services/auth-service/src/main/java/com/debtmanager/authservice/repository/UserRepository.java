package com.debtmanager.authservice.repository;

import com.debtmanager.authservice.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para usuarios.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Busca un usuario por su email.
     *
     * @param email correo del usuario
     * @return usuario si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca un usuario por email que además esté habilitado.
     *
     * @param email correo del usuario
     * @return usuario habilitado si existe
     */
    Optional<User> findByEmailAndEnabledTrue(String email);
}
