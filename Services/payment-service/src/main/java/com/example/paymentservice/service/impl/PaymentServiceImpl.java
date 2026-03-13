package com.example.paymentservice.service.impl;

import com.example.common.exception.ApiException;
import com.example.paymentservice.dto.request.CreatePaymentRequest;
import com.example.paymentservice.dto.response.DebtResponse;
import com.example.paymentservice.dto.response.PaymentResponse;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.service.DebtClient;
import com.example.paymentservice.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Implementación del servicio de pagos.
 *
 * <p>Principios SOLID aplicados:
 * <ul>
 *   <li>SRP – solo contiene lógica de negocio de pagos.</li>
 *   <li>OCP – nuevas validaciones se agregan sin modificar el flujo existente.</li>
 *   <li>LSP – cumple completamente el contrato de {@link PaymentService}.</li>
 *   <li>DIP – depende de abstracciones: {@link PaymentRepository}, {@link DebtClient}.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final DebtClient debtClient;

    public PaymentServiceImpl(PaymentRepository paymentRepository, DebtClient debtClient) {
        this.paymentRepository = paymentRepository;
        this.debtClient = debtClient;
    }

    // ── Registrar un pago ────────────────────────────────────

    @Override
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        log.info("Registrando pago: debtId={} amount={}", request.debtId(), request.amount());

        // 1. Verificar que la deuda existe en debt-service
        DebtResponse debt = debtClient.findById(request.debtId())
                .orElseThrow(() -> ApiException.notFound(
                        "RES_404",
                        "La deuda con ID " + request.debtId() + " no existe",
                        Map.of("debtId", request.debtId())
                ));

        // 2. Verificar que la deuda no esté completamente pagada
        if ("PAGADA".equalsIgnoreCase(debt.status())) {
            throw ApiException.badRequest(
                    "VAL_400",
                    "La deuda ya fue completamente pagada",
                    Map.of("debtId", request.debtId(), "status", debt.status())
            );
        }

        // 3. Verificar que el monto no supera el saldo pendiente
        BigDecimal remaining = debt.currentBalance();
        if (request.amount().compareTo(remaining) > 0) {
            throw ApiException.badRequest(
                    "VAL_400",
                    "El monto del pago supera el saldo pendiente de la deuda",
                    Map.of(
                            "debtId", request.debtId(),
                            "amountRequested", request.amount(),
                            "remainingBalance", remaining
                    )
            );
        }

        // 4. Persistir el pago
        Payment payment = new Payment(
                request.debtId(),
                request.amount(),
                request.paymentDate(),
                request.note()
        );
        Payment saved = paymentRepository.save(payment);

        // 5. Notificar a debt-service para que actualice saldo y estado
        debtClient.notifyPayment(request.debtId(), request.amount());

        log.info("Pago registrado exitosamente: id={}", saved.getId());
        return PaymentResponse.from(saved);
    }

    // ── Consultas ────────────────────────────────────────────

    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Override
    public List<PaymentResponse> getPaymentsByDebt(String debtId) {
        List<PaymentResponse> payments = paymentRepository
                .findByDebtIdOrderByPaymentDateDesc(debtId)
                .stream()
                .map(PaymentResponse::from)
                .toList();

        log.debug("Pagos encontrados para deuda {}: {}", debtId, payments.size());
        return payments;
    }

    @Override
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound(
                        "RES_404",
                        "Pago con ID " + id + " no encontrado",
                        Map.of("paymentId", id)
                ));
        return PaymentResponse.from(payment);
    }
}
