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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/debts")
@RequiredArgsConstructor
public class DebtController {

    private final IDebtService debtService;

    /** POST /debts — Crear nueva deuda */
    @PostMapping
    public ResponseEntity<ApiResponse<DebtResponse>> createDebt(
            @Valid @RequestBody CreateDebtRequest request) {
        Debt debt = debtService.createDebt(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(toResponse(debt), TraceIdUtil.getTraceId()));
    }

    /**
     * GET /debts — Todas las deudas (sin filtro) o filtradas por deudor.
     * debtorId es opcional: si se omite, devuelve todas.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DebtResponse>>> getDebts(
            @RequestParam(required = false) String debtorId) {

        List<Debt> debts = (debtorId != null && !debtorId.isBlank())
                ? debtService.getDebtsByDebtorId(debtorId)
                : debtService.getAllDebts();

        List<DebtResponse> response = debts.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(response, TraceIdUtil.getTraceId()));
    }

    /**
     * GET /debts/summary — KPIs para el dashboard.
     * Parámetro opcional ?currency=DOP|USD para filtrar por moneda.
     * DEBE ir ANTES de /{id} para que "summary" no sea tratado como un ID.
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary(
            @RequestParam(required = false) String currency) {

        List<Debt> debts = (currency != null && !currency.isBlank())
                ? debtService.getDebtsByCurrency(currency)
                : debtService.getAllDebts();

        long totalActivas = debts.stream().filter(d -> "ACTIVA".equals(d.getStatus())).count();
        long totalPagadas = debts.stream().filter(d -> "PAGADA".equals(d.getStatus())).count();

        BigDecimal montoTotalActivo = debts.stream()
                .filter(d -> "ACTIVA".equals(d.getStatus()))
                .map(Debt::getCurrentBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montoTotalCobrado = debts.stream()
                .filter(d -> "PAGADA".equals(d.getStatus()))
                .map(Debt::getOriginalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalActivas", totalActivas);
        summary.put("totalPagadas", totalPagadas);
        summary.put("montoTotalActivo", montoTotalActivo);
        summary.put("montoTotalCobrado", montoTotalCobrado);

        return ResponseEntity.ok(ApiResponse.ok(summary, TraceIdUtil.getTraceId()));
    }

    /** GET /debts/{id} — Detalle de una deuda */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DebtResponse>> getDebtById(@PathVariable String id) {
        Debt debt = debtService.getDebtById(id);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(debt), TraceIdUtil.getTraceId()));
    }

    /** POST /debts/{id}/apply-payment — Endpoint interno del payment-service */
    @PostMapping("/{id}/apply-payment")
    public ResponseEntity<ApiResponse<DebtResponse>> applyPayment(
            @PathVariable String id,
            @Valid @RequestBody ApplyPaymentRequest request) {
        Debt debt = debtService.applyPayment(id, request);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(debt), TraceIdUtil.getTraceId()));
    }

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
