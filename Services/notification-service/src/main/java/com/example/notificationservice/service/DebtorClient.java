package com.example.notificationservice.service;

import com.example.notificationservice.dto.response.DebtorDto;

import java.util.Optional;

/**
 * Contrato para el cliente HTTP que se comunica con {@code debtor-service}.
 *
 * <p>Principio ISP: expone únicamente la consulta de datos de un deudor
 * por su ID, que es lo que {@code notification-service} necesita.
 */
public interface DebtorClient {

    /**
     * Recupera los datos de un deudor por su ID desde {@code debtor-service}.
     *
     * @param debtorId ID del deudor (Long como String)
     * @return {@link Optional} con el deudor encontrado, o vacío si no existe
     */
    Optional<DebtorDto> findById(String debtorId);
}
