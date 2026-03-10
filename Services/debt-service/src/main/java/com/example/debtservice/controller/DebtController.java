package com.example.debtservice.controller;

import com.example.common.api.ApiResponse;
import com.example.common.trace.TraceIdUtil;
import com.example.debtservice.dto.ApplyPaymentRequest;
import com.example.debtservice.dto.CreateDebtRequest;
import com.example.debtservice.dto.DebtResponse;
import com.example.debtservice.entity.Debt;
import com.example.debtservice.service.IDebtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST del microservicio de deudas.
 * Expone los endpoints HTTP para gestionar deudas.
 * Todos los endpoints requieren validación JWT excepto los internos.
 */
@RestController
@RequestMapping("/api/v1/debts")
@RequiredArgsConstructor
public class DebtController {

    /** Servicio de deudas inyectado por interfaz (Dependency Inversion) */
    private final IDebtService debtService;

    /**
     * Crea una nueva deuda.
     * POST /debts
     * Requiere JWT válido en el header Authorization.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DebtResponse>> createDebt(
            @Valid @RequestBody CreateDebtRequest request) {

        Debt debt = debtService.createDebt(request);
        DebtResponse response = toResponse(debt);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, TraceIdUtil.getTraceId()));
    }

    /**
     * Obtiene todas las deudas de un deudor.
     * GET /debts?debtorId=...
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DebtResponse>>> getDebtsByDebtorId(
            @RequestParam String debtorId) {

        List<DebtResponse> debts = debtService.getDebtsByDebtorId(debtorId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(debts, TraceIdUtil.getTraceId()));
    }

    /**
     * Obtiene una deuda por su ID.
     * GET /debts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DebtResponse>> getDebtById(
            @PathVariable String id) {

        Debt debt = debtService.getDebtById(id);

        return ResponseEntity.ok(ApiResponse.ok(toResponse(debt), TraceIdUtil.getTraceId()));
    }

    /**
     * Aplica un pago a una deuda.
     * POST /debts/{id}/apply-payment
     * Endpoint interno llamado por el payment-service.
     */
    @PostMapping("/{id}/apply-payment")
    public ResponseEntity<ApiResponse<DebtResponse>> applyPayment(
            @PathVariable String id,
            @Valid @RequestBody ApplyPaymentRequest request) {

        Debt debt = debtService.applyPayment(id, request);

        return ResponseEntity.ok(ApiResponse.ok(toResponse(debt), TraceIdUtil.getTraceId()));
    }

    /**
     * Convierte una entidad Debt a un DTO DebtResponse.
     * Evita exponer la entidad directamente al cliente.
     */
    private DebtResponse toResponse(Debt debt) {
        return DebtResponse.builder()
                .id(debt.getId())
                .debtorId(debt.getDebtorId())
                .description(debt.getDescription())
                .originalAmount(debt.getOriginalAmount())
                .currentBalance(debt.getCurrentBalance())
                .currency(debt.getCurrency())
                .status(debt.getStatus())
                .dueDate(debt.getDueDate())
                .createdAt(debt.getCreatedAt())
                .updatedAt(debt.getUpdatedAt())
                .build();
    }
}
