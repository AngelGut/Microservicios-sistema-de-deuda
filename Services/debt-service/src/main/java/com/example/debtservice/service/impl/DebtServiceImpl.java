package com.example.debtservice.service.impl;

import com.example.debtservice.dto.ApplyPaymentRequest;
import com.example.debtservice.dto.CreateDebtRequest;
import com.example.debtservice.entity.Debt;
import com.example.debtservice.repository.DebtRepository;
import com.example.debtservice.service.IDebtService;
import com.example.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementación concreta de IDebtService.
 * Contiene toda la lógica de negocio del microservicio de deudas.
 *
 * @Service le indica a Spring que esta clase es un componente de servicio.
 * @RequiredArgsConstructor inyecta automáticamente DebtRepository por
 *                          constructor.
 */
@Service
@RequiredArgsConstructor
public class DebtServiceImpl implements IDebtService {

    /** Repositorio para acceder a la base de datos de deudas */
    private final DebtRepository debtRepository;

    /**
     * Crea una nueva deuda en el sistema.
     * El id se genera automáticamente con UUID.
     * El currentBalance inicia igual al originalAmount.
     * El status inicia como ACTIVA.
     */
    @Override
    public Debt createDebt(CreateDebtRequest request) {

        // Construimos la entidad Debt a partir del request
        Debt debt = Debt.builder()
                .id(UUID.randomUUID().toString()) // Generamos ID único
                .debtorId(request.getDebtorId()) // ID del deudor
                .description(request.getDescription()) // Descripción
                .originalAmount(request.getOriginalAmount()) // Monto original
                .currentBalance(request.getOriginalAmount()) // Balance = monto original al inicio
                .currency(request.getCurrency()) // Moneda
                .status("ACTIVA") // Toda deuda nueva inicia ACTIVA
                .dueDate(request.getDueDate()) // Fecha límite (puede ser null)
                .createdAt(LocalDateTime.now()) // Fecha de creación
                .updatedAt(LocalDateTime.now()) // Fecha de actualización
                .build();

        // Guardamos la deuda en la base de datos y la retornamos
        return debtRepository.save(debt);
    }

    /**
     * Obtiene todas las deudas de un deudor específico.
     * Si el deudor no tiene deudas, retorna lista vacía.
     */

    @Override
    public List<Debt> getAllDebts() {
        return debtRepository.findAll();
    }

    @Override
    public List<Debt> getDebtsByDebtorId(String debtorId) {
        // Usamos el método que definimos en DebtRepository
        return debtRepository.findByDebtorId(debtorId);
    }

    @Override
    public List<Debt> getDebtsByCurrency(String currency) {
        return debtRepository.findByCurrency(currency);
    }

    /**
     * Obtiene una deuda por su ID.
     * Si no existe lanza una excepción 404 usando ApiException de common-lib.
     */
    @Override
    public Debt getDebtById(String id) {
        // Buscamos la deuda, si no existe lanzamos error 404
        return debtRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound(
                        "DEBT_NOT_FOUND",
                        "Deuda no encontrada con id: " + id));
    }

    /**
     * Aplica un pago a una deuda reduciendo el currentBalance.
     * Si el balance llega a 0, el status cambia a PAGADA.
     * Si la deuda ya está PAGADA, lanza error 400.
     * Si el monto del pago es mayor al balance, lanza error 400.
     */
    @Override
    public Debt applyPayment(String id, ApplyPaymentRequest request) {

        // Buscamos la deuda, si no existe lanzamos error 404
        Debt debt = getDebtById(id);

        // Validamos que la deuda no esté ya pagada
        if ("PAGADA".equals(debt.getStatus())) {
            throw ApiException.badRequest(
                    "DEBT_ALREADY_PAID",
                    "La deuda ya está pagada");
        }

        // Validamos que el monto del pago no sea mayor al balance actual
        if (request.getAmount().compareTo(debt.getCurrentBalance()) > 0) {
            throw ApiException.badRequest(
                    "INVALID_PAYMENT_AMOUNT",
                    "El monto del pago no puede ser mayor al balance actual: " + debt.getCurrentBalance());
        }

        // Restamos el monto del pago al balance actual
        BigDecimal newBalance = debt.getCurrentBalance().subtract(request.getAmount());
        debt.setCurrentBalance(newBalance);

        // Si el balance llega a 0, la deuda queda PAGADA
        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            debt.setStatus("PAGADA");
        }

        // Actualizamos la fecha de modificación
        debt.setUpdatedAt(LocalDateTime.now());

        // Guardamos y retornamos la deuda actualizada
        return debtRepository.save(debt);
    }
}
