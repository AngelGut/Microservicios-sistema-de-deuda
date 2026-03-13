package com.example.paymentservice;

import com.example.common.exception.ApiException;
import com.example.paymentservice.dto.request.CreatePaymentRequest;
import com.example.paymentservice.dto.response.DebtResponse;
import com.example.paymentservice.dto.response.PaymentResponse;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.service.DebtClient;
import com.example.paymentservice.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link PaymentServiceImpl}.
 *
 * <p>Se mockean todas las dependencias externas para aislar
 * la lógica de negocio del servicio.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private DebtClient debtClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private static final String DEBT_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String DEBTOR_ID = "debtor-uuid-001";

    private DebtResponse activeDebt;
    private CreatePaymentRequest validRequest;

    @BeforeEach
    void setUp() {
        activeDebt = new DebtResponse(DEBT_ID, DEBTOR_ID, "Deuda de prueba",
                new BigDecimal("1000.00"), new BigDecimal("500.00"), "USD", "PENDIENTE");
        validRequest = new CreatePaymentRequest(DEBT_ID, new BigDecimal("200.00"), LocalDate.now(), "Pago parcial");
    }

    // ── createPayment: casos exitosos ────────────────────────

    @Test
    @DisplayName("Debe registrar un pago válido exitosamente")
    void createPayment_success() {
        when(debtClient.findById(DEBT_ID)).thenReturn(Optional.of(activeDebt));
        Payment savedPayment = new Payment(DEBT_ID, new BigDecimal("200.00"), LocalDate.now(), "Pago parcial");
        when(paymentRepository.save(any())).thenReturn(savedPayment);

        PaymentResponse result = paymentService.createPayment(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.debtId()).isEqualTo(DEBT_ID);
        assertThat(result.amount()).isEqualByComparingTo("200.00");

        verify(debtClient).notifyPayment(eq(DEBT_ID), eq(new BigDecimal("200.00")));
    }

    @Test
    @DisplayName("Debe pasar el monto exacto al saldo pendiente")
    void createPayment_exactRemainingBalance_success() {
        CreatePaymentRequest exactRequest =
                new CreatePaymentRequest(DEBT_ID, new BigDecimal("500.00"), LocalDate.now(), null);

        when(debtClient.findById(DEBT_ID)).thenReturn(Optional.of(activeDebt));
        Payment saved = new Payment(DEBT_ID, new BigDecimal("500.00"), LocalDate.now(), null);
        when(paymentRepository.save(any())).thenReturn(saved);

        assertThatCode(() -> paymentService.createPayment(exactRequest))
                .doesNotThrowAnyException();
    }

    // ── createPayment: validaciones de negocio ───────────────

    @Test
    @DisplayName("Debe lanzar excepción cuando la deuda no existe")
    void createPayment_debtNotFound_throwsException() {
        when(debtClient.findById(DEBT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createPayment(validRequest))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("no existe");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la deuda ya está pagada")
    void createPayment_debtAlreadyPaid_throwsException() {
        DebtResponse paidDebt = new DebtResponse(DEBT_ID, DEBTOR_ID, "Deuda pagada",
                new BigDecimal("1000.00"), BigDecimal.ZERO, "USD", "PAGADA");
        when(debtClient.findById(DEBT_ID)).thenReturn(Optional.of(paidDebt));

        assertThatThrownBy(() -> paymentService.createPayment(validRequest))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("completamente pagada");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el monto supera el saldo pendiente")
    void createPayment_amountExceedsBalance_throwsException() {
        CreatePaymentRequest overRequest =
                new CreatePaymentRequest(DEBT_ID, new BigDecimal("600.00"), LocalDate.now(), null);

        when(debtClient.findById(DEBT_ID)).thenReturn(Optional.of(activeDebt));

        assertThatThrownBy(() -> paymentService.createPayment(overRequest))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("saldo pendiente");

        verify(paymentRepository, never()).save(any());
    }

    // ── getPaymentById ───────────────────────────────────────

    @Test
    @DisplayName("Debe lanzar excepción cuando el pago no existe por ID")
    void getPaymentById_notFound_throwsException() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentById(99L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("no encontrado");
    }
}
