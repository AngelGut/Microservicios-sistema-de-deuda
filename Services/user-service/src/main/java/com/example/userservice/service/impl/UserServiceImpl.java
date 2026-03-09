package com.example.userservice.service.impl;

import com.example.common.exception.ApiException;
import com.example.userservice.dto.*;
import com.example.userservice.entity.Role;
import com.example.userservice.entity.User;
import com.example.userservice.repository.RoleRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación concreta del servicio de usuarios.
 * Contiene toda la lógica de negocio para gestionar usuarios y roles.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Crea un nuevo usuario validando que el username y email no estén en uso.
     * Encripta la contraseña antes de guardarla.
     */
    @Override
    public UserResponse createUser(CreateUserRequest request) {

        // Validamos que el username no esté en uso
        if (userRepository.existsByUsername(request.getUsername())) {
            throw ApiException.badRequest("USERNAME_TAKEN", "El username ya está en uso");
        }

        // Validamos que el email no esté en uso
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApiException.badRequest("EMAIL_TAKEN", "El email ya está registrado");
        }

        // Buscamos los roles por sus IDs
        Set<Role> roles = resolveRoles(request.getRoleIds());

        // Construimos el usuario
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .active(true)
                .roles(roles)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Guardamos en la BD
        User saved = userRepository.save(user);

        return toResponse(saved);
    }

    /**
     * Obtiene todos los usuarios del sistema.
     */
    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Obtiene un usuario por su ID.
     * Lanza excepción si no existe.
     */
    @Override
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "Usuario no encontrado"));
        return toResponse(user);
    }

    /**
     * Actualiza los datos de un usuario existente.
     * Solo actualiza los campos que lleguen en el request.
     */
    @Override
    public UserResponse updateUser(String id, UpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "Usuario no encontrado"));

        // Actualizamos solo los campos que lleguen
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            user.setRoles(resolveRoles(request.getRoleIds()));
        }

        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        user.setUpdatedAt(LocalDateTime.now());

        User updated = userRepository.save(user);
        return toResponse(updated);
    }

    /**
     * Desactiva un usuario del sistema.
     * No lo elimina, solo lo marca como inactivo.
     */
    @Override
    public void deactivateUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "Usuario no encontrado"));

        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Obtiene todos los roles disponibles en el sistema.
     */
    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(this::toRoleResponse)
                .toList();
    }

    /**
     * Busca y valida los roles por sus IDs.
     * Lanza excepción si algún ID no existe.
     */
    private Set<Role> resolveRoles(Set<String> roleIds) {
        Set<Role> roles = new HashSet<>();
        for (String roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> ApiException.notFound("ROLE_NOT_FOUND",
                            "Rol no encontrado con ID: " + roleId));
            roles.add(role);
        }
        return roles;
    }

    /**
     * Convierte una entidad User a un DTO UserResponse.
     */
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .active(user.isActive())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Convierte una entidad Role a un DTO RoleResponse.
     */
    private RoleResponse toRoleResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .build();
    }
}
