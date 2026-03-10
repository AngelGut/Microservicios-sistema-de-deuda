package com.example.airiskservice.service.impl;

import com.example.airiskservice.client.GroqAiAnalyzer;
import com.example.airiskservice.client.PaymentClient;
import com.example.airiskservice.dto.response.GroqRiskResponse;
import com.example.airiskservice.dto.response.PaymentHistoryDTO;
import com.example.airiskservice.dto.response.RiskCalculationResult;
import com.example.airiskservice.dto.response.RiskResponse;
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
import java.util.List;

/**
 * Implementación del servicio de riesgo con doble análisis:
 *   1. Reglas de negocio (matemáticas) — siempre se ejecuta
 *   2. Groq AI (llama3-70b-8192)       — complementa las reglas
 *
 * Estrategia de combinación:
 *   - Si ambos coinciden → resultado definitivo con alta confianza
 *   - Si difieren → se toma el más conservador (mayor riesgo)
 *   - Si Groq falla → solo se usa el resultado de reglas (fallback)
 *
 * Principios SOLID:
 *   SRP → solo orquesta el análisis de riesgo
 *   OCP → nueva lógica de IA se añade sin modificar reglas
 *   LSP → cumple el contrato de RiskService
 *   DIP → depende de PaymentClient y GroqAiAnalyzer (abstracciones)
 */
@Service
@Transactional(readOnly = true)
public class RiskServiceImpl implements RiskService {

    private static final Logger log = LoggerFactory.getLogger(RiskServiceImpl.class);
    private static final int HIGH_RISK_THRESHOLD = 30;

    private final ClientRiskRepository clientRiskRepository;
    private final PaymentClient paymentClient;
    private final GroqAiAnalyzer groqAiAnalyzer;

    public RiskServiceImpl(ClientRiskRepository clientRiskRepository,
                           PaymentClient paymentClient,
                           GroqAiAnalyzer groqAiAnalyzer) {
        this.clientRiskRepository = clientRiskRepository;
        this.paymentClient        = paymentClient;
        this.groqAiAnalyzer       = groqAiAnalyzer;
    }

    // ── Consulta ─────────────────────────────────────────────

    @Override
    @Cacheable(value = "clientRisk", key = "#clientId")
    public RiskResponse getRiskByClient(Long clientId) {
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

    // ── Recálculo con doble análisis ─────────────────────────

    @Override
    @Transactional
    @CacheEvict(value = "clientRisk", key = "#clientId")
    public RiskResponse recalculate(Long clientId) {
        log.info("Iniciando doble análisis de riesgo para clientId={}", clientId);

        // PASO 1 — Obtener historial de pagos
        List<PaymentHistoryDTO> payments = paymentClient.getPaymentsByClient(clientId);

        // PASO 2 — Análisis con reglas de negocio
        RiskCalculationResult rulesResult = applyBusinessRules(clientId, payments);
        log.info("[REGLAS] clientId={} → level={} score={}",
                clientId, rulesResult.riskLevel(), rulesResult.riskScore());

        // PASO 3 — Análisis con Groq AI
        GroqRiskResponse aiResult = groqAiAnalyzer.analyze(
                clientId,
                rulesResult.totalDaysLate(),
                rulesResult.latePaymentCount(),
                rulesResult.paymentCount(),
                payments
        );

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

    // ── Reglas de negocio ────────────────────────────────────

    private RiskCalculationResult applyBusinessRules(Long clientId,
                                                      List<PaymentHistoryDTO> payments) {
        int totalDaysLate = 0;
        int lateCount     = 0;
        int paymentCount  = payments.size();

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
        Double score    = calculateRulesScore(totalDaysLate, lateCount, paymentCount);

        return new RiskCalculationResult(
                clientId, level, score, totalDaysLate, lateCount, paymentCount);
    }

    private RiskLevel classifyByRules(int totalDaysLate) {
        if (totalDaysLate == 0)                  return RiskLevel.GOOD_CLIENT;
        if (totalDaysLate < HIGH_RISK_THRESHOLD) return RiskLevel.LOW_RISK;
        return RiskLevel.HIGH_RISK;
    }

    private Double calculateRulesScore(int totalDaysLate, int lateCount, int total) {
        if (total == 0) return 0.0;
        double lateRatio  = (double) lateCount / total;
        double daysScore  = Math.min(totalDaysLate, 100.0);
        return Math.min((lateRatio * 50) + (daysScore * 0.5), 100.0);
    }

    // ── Combinación de resultados ─────────────────────────────

    /**
     * Estrategia de combinación:
     *   - Si Groq no está disponible → 100% reglas
     *   - Si coinciden               → promedio de scores, mismo nivel
     *   - Si difieren                → nivel más conservador (mayor riesgo),
     *                                  promedio de scores ponderado 60/40
     */
    private RiskCalculationResult combineResults(RiskCalculationResult rules,
                                                  GroqRiskResponse ai) {
        if (ai == null) {
            log.warn("Groq no disponible, usando solo resultado de reglas.");
            return rules;
        }

        RiskLevel finalLevel;
        double finalScore;

        if (rules.riskLevel() == ai.riskLevel()) {
            // Ambos coinciden — alta confianza
            finalLevel = rules.riskLevel();
            finalScore = (rules.riskScore() + ai.aiScore()) / 2.0;
            log.info("Ambos análisis coinciden en nivel={}", finalLevel);
        } else {
            // Difieren — tomar el más conservador (mayor riesgo)
            finalLevel = higherRisk(rules.riskLevel(), ai.riskLevel());
            // Ponderación: 60% reglas + 40% IA
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
                rules.paymentCount()
        );
    }

    /** Devuelve el nivel de mayor riesgo entre dos niveles. */
    private RiskLevel higherRisk(RiskLevel a, RiskLevel b) {
        int rankA = riskRank(a);
        int rankB = riskRank(b);
        return rankA >= rankB ? a : b;
    }

    private int riskRank(RiskLevel level) {
        return switch (level) {
            case GOOD_CLIENT -> 0;
            case LOW_RISK    -> 1;
            case HIGH_RISK   -> 2;
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
