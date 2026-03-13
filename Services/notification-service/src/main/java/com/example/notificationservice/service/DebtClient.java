package com.example.notificationservice.service;

import com.example.notificationservice.dto.response.DebtDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Contrato para el cliente HTTP que se comunica con {@code debt-service}.
 *
 * <p>Principio ISP: solo expone las operaciones que
 * {@code notification-service} necesita de {@code debt-service}.
 *
 * <p>Principio DIP: los consumidores dependen de esta interfaz,
 * permitiendo sustituir la implementación (ej. cambio de protocolo)
 * sin modificar el resto del sistema.
 */
public interface DebtClient {

    /**
     * Busca una deuda por su ID en {@code debt-service}.
     *
     * @param debtId ID único de la deuda (UUID)
     * @return {@link Optional} con la deuda encontrada, o vacío si no existe
     */
    Optional<DebtDto> findById(String debtId);

    /**
     * Consulta todas las deudas cuya fecha de vencimiento sea igual a
     * la fecha indicada y que estén en estado ACTIVA.
     *
     * <p>Usado por el scheduler de recordatorios para detectar deudas
     * que vencen en exactamente N días.
     *
     * @param dueDate fecha de vencimiento a consultar
     * @return lista de deudas que cumplen el criterio
     */
    List<DebtDto> findActiveDebtsWithDueDate(LocalDate dueDate);
}
