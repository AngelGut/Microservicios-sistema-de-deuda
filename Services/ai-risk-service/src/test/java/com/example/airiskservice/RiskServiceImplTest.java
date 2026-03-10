package com.example.airiskservice;

import com.example.airiskservice.client.GroqAiAnalyzer;
import com.example.airiskservice.client.PaymentClient;
import com.example.airiskservice.dto.response.GroqRiskResponse;
import com.example.airiskservice.dto.response.PaymentHistoryDTO;
import com.example.airiskservice.dto.response.RiskResponse;
import com.example.airiskservice.model.ClientRisk;
import com.example.airiskservice.model.RiskLevel;
import com.example.airiskservice.repository.ClientRiskRepository;
import com.example.airiskservice.service.impl.RiskServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskServiceImplTest {

    @Mock private ClientRiskRepository repo;
    @Mock private PaymentClient paymentClient;
    @Mock private GroqAiAnalyzer groqAiAnalyzer;
    @InjectMocks private RiskServiceImpl service;

    // ── Helper ────────────────────────────────────────────────

    private PaymentHistoryDTO payment(LocalDate paymentDate, LocalDate dueDate) {
        return new PaymentHistoryDTO(1L, 1L, 10L,
                BigDecimal.valueOf(500), paymentDate, dueDate, null);
    }

    private GroqRiskResponse groqResponse(RiskLevel level, double score) {
        return new GroqRiskResponse(level, score,
                List.of("Monitorear al cliente", "Solicitar garantía"), "raw");
    }

    // ── Tests de reglas ──────────────────────────────────────

    @Test
    @DisplayName("Sin mora → GOOD_CLIENT (reglas y IA coinciden)")
    void noLatePayments_bothAgree_goodClient() {
        when(paymentClient.getPaymentsByClient(1L))
                .thenReturn(List.of(payment(
                        LocalDate.of(2026, 3, 1),
                        LocalDate.of(2026, 3, 1))));
        when(groqAiAnalyzer.analyze(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(groqResponse(RiskLevel.GOOD_CLIENT, 0.0));
        when(repo.findByClientId(1L)).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        RiskResponse result = service.recalculate(1L);

        assertThat(result.riskLevel()).isEqualTo(RiskLevel.GOOD_CLIENT);
        assertThat(result.aiRiskLevel()).isEqualTo(RiskLevel.GOOD_CLIENT);
        assertThat(result.aiRecommendations()).isNotNull();
    }

    @Test
    @DisplayName("9 días de mora → LOW_RISK")
    void nineDaysLate_lowRisk() {
        when(paymentClient.getPaymentsByClient(2L))
                .thenReturn(List.of(payment(
                        LocalDate.of(2026, 3, 10),
                        LocalDate.of(2026, 3, 1))));
        when(groqAiAnalyzer.analyze(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(groqResponse(RiskLevel.LOW_RISK, 15.0));
        when(repo.findByClientId(2L)).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        RiskResponse result = service.recalculate(2L);

        assertThat(result.riskLevel()).isEqualTo(RiskLevel.LOW_RISK);
        assertThat(result.totalDaysLate()).isEqualTo(9);
    }

    @Test
    @DisplayName("31 días de mora → HIGH_RISK")
    void thirtyOneDaysLate_highRisk() {
        when(paymentClient.getPaymentsByClient(3L))
                .thenReturn(List.of(payment(
                        LocalDate.of(2026, 4, 1),
                        LocalDate.of(2026, 3, 1))));
        when(groqAiAnalyzer.analyze(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(groqResponse(RiskLevel.HIGH_RISK, 75.0));
        when(repo.findByClientId(3L)).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        RiskResponse result = service.recalculate(3L);

        assertThat(result.riskLevel()).isEqualTo(RiskLevel.HIGH_RISK);
    }

    @Test
    @DisplayName("Groq no disponible → solo reglas (fallback)")
    void groqUnavailable_fallbackToRules() {
        when(paymentClient.getPaymentsByClient(4L))
                .thenReturn(List.of(payment(
                        LocalDate.of(2026, 3, 10),
                        LocalDate.of(2026, 3, 1))));
        when(groqAiAnalyzer.analyze(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(null);  // Groq caído
        when(repo.findByClientId(4L)).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        RiskResponse result = service.recalculate(4L);

        assertThat(result.riskLevel()).isEqualTo(RiskLevel.LOW_RISK);
        assertThat(result.aiRiskLevel()).isNull();     // sin datos de IA
        assertThat(result.aiRecommendations()).isNull();
    }

    @Test
    @DisplayName("Reglas y Groq difieren → se toma el mayor riesgo")
    void rulesAndGroqDisagree_takesHigherRisk() {
        // Reglas dicen LOW_RISK (9 días), IA dice HIGH_RISK
        when(paymentClient.getPaymentsByClient(5L))
                .thenReturn(List.of(payment(
                        LocalDate.of(2026, 3, 10),
                        LocalDate.of(2026, 3, 1))));
        when(groqAiAnalyzer.analyze(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(groqResponse(RiskLevel.HIGH_RISK, 80.0));
        when(repo.findByClientId(5L)).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        RiskResponse result = service.recalculate(5L);

        // Debe tomar el más conservador (HIGH_RISK)
        assertThat(result.riskLevel()).isEqualTo(RiskLevel.HIGH_RISK);
    }
}
