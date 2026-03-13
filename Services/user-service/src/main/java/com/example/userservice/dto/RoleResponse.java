package com.example.userservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO para devolver la información de un rol al cliente.
 */
@Data
@Builder
public class RoleResponse {

    private String id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}
