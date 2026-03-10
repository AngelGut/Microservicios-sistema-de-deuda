package com.example.userservice.service;

import com.example.userservice.dto.*;

import java.util.List;

/**
 * Interfaz que define el contrato de operaciones del servicio de usuarios.
 * Siguiendo el principio de Inversión de Dependencias (SOLID - D),
 * el controlador dependerá de esta interfaz y no de la implementación concreta.
 */
public interface IUserService {

    /** Crea un nuevo usuario con sus roles asignados */
    UserResponse createUser(CreateUserRequest request);

    /** Obtiene todos los usuarios del sistema */
    List<UserResponse> getAllUsers();

    /** Obtiene un usuario por su ID */
    UserResponse getUserById(String id);

    /** Actualiza los datos de un usuario existente */
    UserResponse updateUser(String id, UpdateUserRequest request);

    /** Desactiva un usuario del sistema */
    void deactivateUser(String id);

    /** Obtiene todos los roles disponibles */
    List<RoleResponse> getAllRoles();
}
