package com.example.airiskservice.service.impl;

import com.example.airiskservice.client.DebtClient;
import com.example.airiskservice.client.GroqAiAnalyzer;
import com.example.airiskservice.client.PaymentClient;
import com.example.airiskservice.dto.response.*;
import com.example.airiskservice.model.ClientRisk;
import com.example.airiskservice.model.RiskLevel;
import com.example.airiskservice.repository.ClientRiskRepository;
import com.example.airiskservice.service.RiskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RiskServiceImpl implements RiskService {

    private static final Logger log = LoggerFactory.getLogger(RiskServiceImpl.class);
    private static final int HIGH_RISK_THRESHOLD = 30;

    private final ClientRiskRepository clientRiskRepository;
    private final PaymentClient paymentClient;
    private final DebtClient debtClient;
    private final GroqAiAnalyzer groqAiAnalyzer;

    public RiskServiceImpl(ClientRiskRepository clientRiskRepository,
            PaymentClient paymentClient,
            DebtClient debtClient,
            GroqAiAnalyzer groqAiAnalyzer) {
        this.clientRiskRepository = clientRiskRepository;
        this.paymentClient = paymentClient;
        this.debtClient = debtClient;
        this.groqAiAnalyzer = groqAiAnalyzer;
    }

    @Override
    @Cacheable(value = "clientRisk", key = "#clientId")
    public RiskResponse getRiskByClient(String clientId) {
        return clientRiskRepository.findByClientId(clientId)
                .map(RiskResponse::from)
                .orElseGet(() -> {
                    log.info("Sin perfil de riesgo para clientId={}, calculando...", clientId);
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

    @Override
    @Transactional
    @CacheEvict(value = "clientRisk", key = "#clientId")
    public RiskResponse recalculate(String clientId) {
        log.info("Iniciando doble análisis de riesgo para clientId={}", clientId);

        // PASO 1 — Construir historial combinando debt-service + payment-service
        List<PaymentHistoryDTO> history = buildPaymentHistory(clientId);
        log.info("Historial construido: {} registros para clientId={}", history.size(), clientId);

        // PASO 2 — Análisis con reglas de negocio
        RiskCalculationResult rulesResult = applyBusinessRules(clientId, history);
        log.info("[REGLAS] clientId={} → level={} score={}",
                clientId, rulesResult.riskLevel(), rulesResult.riskScore());

        // PASO 3 — Análisis con Groq AI
        GroqRiskResponse aiResult = groqAiAnalyzer.analyze(
                clientId,
                rulesResult.totalDaysLate(),
                rulesResult.latePaymentCount(),
                rulesResult.paymentCount(),
                history);

        // PASO 4 — Combinar resultados
        RiskCalculationResult finalResult = combineResults(rulesResult, aiResult);
        log.info("[FINAL]  clientId={} → level={} score={} (IA disponible: {})",
                clientId, finalResult.riskLevel(), finalResult.riskScore(), aiResult != null);

        // PASO 5 — Persistir
        ClientRisk entity = clientRiskRepository.findByClientId(clientId)
                .orElse(new ClientRisk(clientId));
        applyResult(entity, finalResult);
        clientRiskRepository.save(entity);

        return RiskResponse.from(entity, aiResult);
    }

    @Override
    @Transactional
    public void recalculateAll() {
        log.info("Recálculo masivo de riesgo iniciado...");
        clientRiskRepository.findAll()
                .forEach(cr -> recalculate(cr.getClientId()));
        log.info("Recálculo masivo completado.");
    }

    // ── Construcción del historial ───────────────────────────

    private List<PaymentHistoryDTO> buildPaymentHistory(String debtorId) {
        List<DebtDTO> debts = debtClient.getDebtsByDebtor(debtorId).getData();
        List<PaymentHistoryDTO> history = new ArrayList<>();

        for (DebtDTO debt : debts) {
            List<PaymentDTO> payments = paymentClient.getPaymentsByDebt(debt.id()).getData();
            for (PaymentDTO payment : payments) {
                history.add(new PaymentHistoryDTO(
                        String.valueOf(payment.id()),
                        debt.id(),
                        debtorId,
                        payment.amount(),
                        payment.paymentDate(),
                        debt.dueDate(),
                        payment.note()));
            }
        }

        return history;
    }

    // ── Reglas de negocio ────────────────────────────────────

    private RiskCalculationResult applyBusinessRules(String clientId,
            List<PaymentHistoryDTO> payments) {
        int totalDaysLate = 0;
        int lateCount = 0;
        int paymentCount = payments.size();

        for (PaymentHistoryDTO p : payments) {
            if (p.dueDate() != null && p.paymentDate() != null) {
                long daysLate = ChronoUnit.DAYS.between(p.dueDate(), p.paymentDate());
                if (daysLate > 0) {
                    totalDaysLate += (int) daysLate;
                    lateCount++;
                }
            }
        }

        RiskLevel level = classifyByRules(totalDaysLate);
        Double score = calculateRulesScore(totalDaysLate, lateCount, paymentCount);

        return new RiskCalculationResult(
                clientId, level, score, totalDaysLate, lateCount, paymentCount);
    }

    private RiskLevel classifyByRules(int totalDaysLate) {
        if (totalDaysLate == 0)
            return RiskLevel.GOOD_CLIENT;
        if (totalDaysLate < HIGH_RISK_THRESHOLD)
            return RiskLevel.LOW_RISK;
        return RiskLevel.HIGH_RISK;
    }

    private Double calculateRulesScore(int totalDaysLate, int lateCount, int total) {
        if (total == 0)
            return 0.0;
        double lateRatio = (double) lateCount / total;
        double daysScore = Math.min(totalDaysLate, 100.0);
        return Math.min((lateRatio * 50) + (daysScore * 0.5), 100.0);
    }

    // ── Combinación de resultados ─────────────────────────────

    private RiskCalculationResult combineResults(RiskCalculationResult rules,
            GroqRiskResponse ai) {
        if (ai == null) {
            log.warn("Groq no disponible, usando solo resultado de reglas.");
            return rules;
        }

        RiskLevel finalLevel;
        double finalScore;

        if (rules.riskLevel() == ai.riskLevel()) {
            finalLevel = rules.riskLevel();
            finalScore = (rules.riskScore() + ai.aiScore()) / 2.0;
            log.info("Ambos análisis coinciden en nivel={}", finalLevel);
        } else {
            finalLevel = higherRisk(rules.riskLevel(), ai.riskLevel());
            finalScore = (rules.riskScore() * 0.6) + (ai.aiScore() * 0.4);
            log.warn("Análisis difieren: reglas={} IA={} → se toma {}",
                    rules.riskLevel(), ai.riskLevel(), finalLevel);
        }

        return new RiskCalculationResult(
                rules.clientId(),
                finalLevel,
                Math.min(finalScore, 100.0),
                rules.totalDaysLate(),
                rules.latePaymentCount(),
                rules.paymentCount());
    }

    private RiskLevel higherRisk(RiskLevel a, RiskLevel b) {
        return riskRank(a) >= riskRank(b) ? a : b;
    }

    private int riskRank(RiskLevel level) {
        return switch (level) {
            case GOOD_CLIENT -> 0;
            case LOW_RISK -> 1;
            case HIGH_RISK -> 2;
        };
    }

    // ── Persistencia ─────────────────────────────────────────

    private void applyResult(ClientRisk entity, RiskCalculationResult result) {
        entity.setRiskLevel(result.riskLevel());
        entity.setRiskScore(result.riskScore());
        entity.setTotalDaysLate(result.totalDaysLate());
        entity.setLatePaymentCount(result.latePaymentCount());
        entity.setPaymentCount(result.paymentCount());
        entity.setLastCalculatedAt(Instant.now());
    }
}
