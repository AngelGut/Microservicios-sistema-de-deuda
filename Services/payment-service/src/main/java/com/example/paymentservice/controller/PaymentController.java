package com.example.paymentservice.controller;

import com.example.common.api.ApiResponse;
import com.example.common.trace.TraceIdUtil;
import com.example.paymentservice.dto.request.CreatePaymentRequest;
import com.example.paymentservice.dto.response.PaymentResponse;
import com.example.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST del microservicio de pagos.
 *
 * <p>
 * Principio SRP: solo delega al servicio y construye las respuestas HTTP.
 * No contiene lógica de negocio.
 */
@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Operaciones de gestión de pagos")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ── POST /payments ───────────────────────────────────────

    @PostMapping
    @Operation(summary = "Registrar un pago", description = "Registra un nuevo pago para una deuda. Valida la existencia de la deuda, "
            +
            "que no esté pagada y que el monto no supere el saldo pendiente.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Pago registrado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validación fallida o monto inválido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Deuda no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token JWT no válido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "debt-service no disponible (circuit breaker abierto)")
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {

        PaymentResponse payment = paymentService.createPayment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(payment, TraceIdUtil.getTraceId()));
    }

    // ── GET /payments ────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Listar todos los pagos", description = "Devuelve el listado completo de pagos registrados.")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPayments() {
        List<PaymentResponse> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(ApiResponse.ok(payments, TraceIdUtil.getTraceId()));
    }

    // ── GET /payments/{id} ───────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un pago por ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pago encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @Parameter(description = "ID del pago") @PathVariable Long id) {

        PaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.ok(payment, TraceIdUtil.getTraceId()));
    }

    // ── GET /payments/by-debt/{debtId} ───────────────────────

    @GetMapping("/by-debt/{debtId}")
    @Operation(summary = "Historial de pagos por deuda", description = "Devuelve todos los pagos de una deuda específica, ordenados por fecha descendente.")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByDebt(
            @Parameter(description = "ID de la deuda") @PathVariable Long debtId) {

        List<PaymentResponse> payments = paymentService.getPaymentsByDebt(debtId);
        return ResponseEntity.ok(ApiResponse.ok(payments, TraceIdUtil.getTraceId()));
    }
}
