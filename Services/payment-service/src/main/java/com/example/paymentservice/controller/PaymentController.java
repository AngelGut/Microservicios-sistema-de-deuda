package com.example.paymentservice.controller;

import com.example.common.api.ApiResponse;
import com.example.common.trace.TraceIdUtil;
import com.example.paymentservice.dto.request.CreatePaymentRequest;
import com.example.paymentservice.dto.response.PaymentResponse;
import com.example.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** POST /api/v1/payments — Registrar un pago */
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse payment = paymentService.createPayment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(payment, TraceIdUtil.getTraceId()));
    }

    /**
     * GET /api/v1/payments — Todos los pagos.
     * Si se pasa ?debtId=X, filtra por deuda.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPayments(
            @RequestParam(required = false) String debtId) {

        List<PaymentResponse> payments = (debtId != null && !debtId.isBlank())
                ? paymentService.getPaymentsByDebt(debtId)
                : paymentService.getAllPayments();

        return ResponseEntity.ok(ApiResponse.ok(payments, TraceIdUtil.getTraceId()));
    }

    /** GET /api/v1/payments/recent?limit=N — Últimos N pagos para el dashboard */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getRecent(
            @RequestParam(defaultValue = "7") int limit) {

        List<PaymentResponse> all = paymentService.getAllPayments();
        List<PaymentResponse> recent = all.stream()
                .sorted((a, b) -> {
                    if (a.createdAt() == null)
                        return 1;
                    if (b.createdAt() == null)
                        return -1;
                    return b.createdAt().compareTo(a.createdAt());
                })
                .limit(limit)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(recent, TraceIdUtil.getTraceId()));
    }

    /** GET /api/v1/payments/{id} — Pago por ID */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        PaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.ok(payment, TraceIdUtil.getTraceId()));
    }

    /**
     * GET /api/v1/payments/by-debt/{debtId} — Pagos de una deuda (mantener
     * compatibilidad)
     */
    @GetMapping("/by-debt/{debtId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByDebt(
            @PathVariable String debtId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByDebt(debtId);
        return ResponseEntity.ok(ApiResponse.ok(payments, TraceIdUtil.getTraceId()));
    }
}
