package com.example.airiskservice.service.impl;

import com.example.airiskservice.client.PaymentClient;
import com.example.airiskservice.dto.response.PaymentHistoryDTO;
import com.example.airiskservice.dto.response.RiskCalculationResult;
import com.example.airiskservice.dto.response.RiskResponse;
import com.example.airiskservice.model.ClientRisk;
import com.example.airiskservice.model.RiskLevel;
import com.example.airiskservice.repository.ClientRiskRepository;
import com.example.airiskservice.service.RiskService;
import com.example.common.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Implementación del servicio de análisis de riesgo crediticio.
 *
 * Principios SOLID aplicados:
 * - SRP: solo contiene lógica de análisis y clasificación de riesgo.
 * - OCP: nuevas reglas de clasificación se añaden sin modificar el flujo existente.
 * - LSP: cumple completamente el contrato de RiskService.
 * - DIP: depende de abstracciones (RiskService, PaymentClient, ClientRiskRepository).
 */
@Service
@Transactional(readOnly = true)
public class RiskServiceImpl implements RiskService {

    private static final Logger log = LoggerFactory.getLogger(RiskServiceImpl.class);

    // Umbral de días de mora para clasificar HIGH_RISK
    private static final int HIGH_RISK_THRESHOLD = 30;

    private final ClientRiskRepository clientRiskRepository;
    private final PaymentClient paymentClient;

    public RiskServiceImpl(ClientRiskRepository clientRiskRepository,
                           PaymentClient paymentClient) {
        this.clientRiskRepository = clientRiskRepository;
        this.paymentClient = paymentClient;
    }

    // ── Consulta de riesgo ───────────────────────────────────

    @Override
    @Cacheable(value = "clientRisk", key = "#clientId")
    public RiskResponse getRiskByClient(Long clientId) {
        return clientRiskRepository.findByClientId(clientId)
                .map(RiskResponse::from)
                .orElseGet(() -> {
                    log.info("No existe perfil de riesgo para clientId={}, calculando...", clientId);
                    return recalculate(clientId);
                });
    }

    @Override
    public List<RiskResponse> getHighRiskClients() {
        return clientRiskRepository.findByRiskLevel(RiskLevel.HIGH_RISK)
                .stream()
                .map(RiskResponse::from)
                .toList();
    }

    // ── Recálculo ────────────────────────────────────────────

    @Override
    @Transactional
    @CacheEvict(value = "clientRisk", key = "#clientId")
    public RiskResponse recalculate(Long clientId) {
        log.info("Recalculando riesgo para clientId={}", clientId);

        // 1. Obtener historial de pagos desde payment-service
        List<PaymentHistoryDTO> payments = paymentClient.getPaymentsByClient(clientId);

        // 2. Calcular métricas de riesgo
        RiskCalculationResult result = calculateRisk(clientId, payments);

        // 3. Persistir o actualizar
        ClientRisk clientRisk = clientRiskRepository.findByClientId(clientId)
                .orElse(new ClientRisk(clientId));

        applyResult(clientRisk, result);
        clientRiskRepository.save(clientRisk);

        log.info("Riesgo calculado: clientId={} level={} score={}",
                clientId, result.riskLevel(), result.riskScore());

        return RiskResponse.from(clientRisk);
    }

    @Override
    @Transactional
    public void recalculateAll() {
        log.info("Recalculando riesgo de todos los clientes...");
        List<ClientRisk> allClients = clientRiskRepository.findAll();
        allClients.forEach(cr -> recalculate(cr.getClientId()));
        log.info("Recálculo completado para {} clientes.", allClients.size());
    }

    // ── Lógica de negocio (privada) ──────────────────────────

    /**
     * Calcula el riesgo basado en el historial de pagos.
     *
     * Regla de mora: days_late = payment_date - due_date (0 si es a tiempo)
     * Reglas de clasificación:
     * - GOOD_CLIENT : totalDaysLate == 0
     * - LOW_RISK    : totalDaysLate > 0 && < 30
     * - HIGH_RISK   : totalDaysLate >= 30
     */
    private RiskCalculationResult calculateRisk(Long clientId,
                                                List<PaymentHistoryDTO> payments) {
        int totalDaysLate = 0;
        int latePaymentCount = 0;
        int paymentCount = payments.size();

        for (PaymentHistoryDTO payment : payments) {
            if (payment.dueDate() != null && payment.paymentDate() != null) {
                long daysLate = ChronoUnit.DAYS.between(
                        payment.dueDate(), payment.paymentDate());

                if (daysLate > 0) {
                    totalDaysLate += (int) daysLate;
                    latePaymentCount++;
                }
            }
        }

        RiskLevel level = classifyRisk(totalDaysLate);
        Double score = calculateScore(totalDaysLate, latePaymentCount, paymentCount);

        return new RiskCalculationResult(
                clientId, level, score, totalDaysLate, latePaymentCount, paymentCount);
    }

    /**
     * Clasifica el nivel de riesgo según los días de mora acumulados.
     * Principio OCP: para nuevas reglas, solo se extiende este método.
     */
    private RiskLevel classifyRisk(int totalDaysLate) {
        if (totalDaysLate == 0)                    return RiskLevel.GOOD_CLIENT;
        if (totalDaysLate < HIGH_RISK_THRESHOLD)   return RiskLevel.LOW_RISK;
        return RiskLevel.HIGH_RISK;
    }

    /**
     * Calcula un score numérico de riesgo entre 0 y 100.
     * Mayor score = mayor riesgo.
     */
    private Double calculateScore(int totalDaysLate, int lateCount, int totalCount) {
        if (totalCount == 0) return 0.0;
        double lateRatio = (double) lateCount / totalCount;
        double daysScore = Math.min(totalDaysLate, 100.0);
        return Math.min((lateRatio * 50) + (daysScore * 0.5), 100.0);
    }

    private void applyResult(ClientRisk clientRisk, RiskCalculationResult result) {
        clientRisk.setRiskLevel(result.riskLevel());
        clientRisk.setRiskScore(result.riskScore());
        clientRisk.setTotalDaysLate(result.totalDaysLate());
        clientRisk.setLatePaymentCount(result.latePaymentCount());
        clientRisk.setPaymentCount(result.paymentCount());
        clientRisk.setLastCalculatedAt(Instant.now());
    }
}
