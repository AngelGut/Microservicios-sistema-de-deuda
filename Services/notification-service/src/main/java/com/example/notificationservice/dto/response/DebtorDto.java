package com.example.notificationservice.dto.response;

/**
 * DTO que representa los datos de un deudor obtenidos desde {@code debtor-service}.
 *
 * <p>Solo contiene los campos necesarios para el envío de notificaciones,
 * respetando el principio ISP: no se exponen propiedades irrelevantes
 * para este contexto.
 *
 * <p>Mapea la respuesta del endpoint:
 * {@code GET /api/v1/debtors/{id}} de debtor-service
 */
public record DebtorDto(

        /** ID único del deudor en debtor-service. */
        Long id,

        /** Nombre completo del deudor. */
        String name,

        /** Correo electrónico para recibir notificaciones. */
        String email,

        /** Documento de identidad (cédula/pasaporte). */
        String document,

        /** Teléfono de contacto. */
        String phone
) {}
