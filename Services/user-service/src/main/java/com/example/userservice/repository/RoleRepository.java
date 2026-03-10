package com.example.userservice.repository;

import com.example.userservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad Role.
 * Extiende JpaRepository para obtener operaciones CRUD gratuitas.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    /** Busca un rol por su nombre */
    Optional<Role> findByName(String name);

    /** Verifica si ya existe un rol con ese nombre */
    boolean existsByName(String name);
}
