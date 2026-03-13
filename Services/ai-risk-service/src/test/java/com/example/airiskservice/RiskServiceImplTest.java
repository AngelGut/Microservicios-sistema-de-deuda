package com.example.airiskservice;

import com.example.airiskservice.client.DebtClient;
import com.example.airiskservice.client.GroqAiAnalyzer;
import com.example.airiskservice.client.PaymentClient;
import com.example.airiskservice.dto.response.*;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskServiceImplTest {

    @Mock
    private ClientRiskRepository repo;
    @Mock
    private PaymentClient paymentClient;
    @Mock
    private DebtClient debtClient;
    @Mock
    private GroqAiAnalyzer groqAiAnalyzer;
    @InjectMocks
    private RiskServiceImpl service;

    // ── Helpers ───────────────────────────────────────────────

    private DebtDTO debt(String id, String debtorId, LocalDate dueDate) {
        return new DebtDTO(id, debtorId, "Deuda test",
                BigDecimal.valueOf(1000), BigDecimal.valueOf(500),
                "DOP", "ACTIVA", dueDate);
    }

    private PaymentDTO payment(String debtId, LocalDate paymentDate) {
        return new PaymentDTO(1L, debtId, BigDecimal.valueOf(500),
                paymentDate, null, LocalDateTime.now());
    }

    private GroqRiskResponse groqResponse(RiskLevel level, double score) {
        return new GroqRiskResponse(level, score,
                List.of("Monitorear al cliente", "Solicitar garantía"), "raw");
    }

    // ── Tests ─────────────────────────────────────────────────

    @Test
    @DisplayName("Sin mora → GOOD_CLIENT (reglas y IA coinciden)")
    void noLatePayments_bothAgree_goodClient() {
        String debtorId = "1";
        String debtId = "debt-1";
        LocalDate dueDate = LocalDate.of(2026, 3, 1);

        when(debtClient.getDebtsByDebtor(debtorId))
                .thenReturn(List.of(debt(debtId, debtorId, dueDate)));
        when(paymentClient.getPaymentsByDebt(debtId))
                .thenReturn(List.of(payment(debtId, dueDate))); // pagó en fecha
        when(groqAiAnalyzer.analyze(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(groqResponse(RiskLevel.GOOD_CLIENT, 0.0));
        when(repo.findByClientId(1L)).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        RiskResponse result = service.recalculate(1L);

        assertThat(result.riskLevel()).isEqualTo(RiskLevel.GOOD_CLIENT);
        assertThat(result.aiRiskLevel()).isEqualTo(RiskLevel.GOOD_CLIENT);
    }

    @Test
    @DisplayName("9 días de mora → LOW_RISK")
    void nineDaysLate_lowRisk() {
        String debtorId = "2";
        String debtId = "debt-2";
        LocalDate dueDate = LocalDate.of(2026, 3, 1);

        when(debtClient.getDebtsByDebtor(debtorId))
                .thenReturn(List.of(debt(debtId, debtorId, dueDate)));
        when(paymentClient.getPaymentsByDebt(debtId))
                .thenReturn(List.of(payment(debtId, dueDate.plusDays(9)))); // 9 días tarde
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
        String debtorId = "3";
        String debtId = "debt-3";
        LocalDate dueDate = LocalDate.of(2026, 3, 1);

        when(debtClient.getDebtsByDebtor(debtorId))
                .thenReturn(List.of(debt(debtId, debtorId, dueDate)));
        when(paymentClient.getPaymentsByDebt(debtId))
                .thenReturn(List.of(payment(debtId, dueDate.plusDays(31)))); // 31 días tarde
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
        String debtorId = "4";
        String debtId = "debt-4";
        LocalDate dueDate = LocalDate.of(2026, 3, 1);

        when(debtClient.getDebtsByDebtor(debtorId))
                .thenReturn(List.of(debt(debtId, debtorId, dueDate)));
        when(paymentClient.getPaymentsByDebt(debtId))
                .thenReturn(List.of(payment(debtId, dueDate.plusDays(9))));
        when(groqAiAnalyzer.analyze(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(null); // Groq caído
        when(repo.findByClientId(4L)).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        RiskResponse result = service.recalculate(4L);

        assertThat(result.riskLevel()).isEqualTo(RiskLevel.LOW_RISK);
        assertThat(result.aiRiskLevel()).isNull();
        assertThat(result.aiRecommendations()).isNull();
    }

    @Test
    @DisplayName("Reglas y Groq difieren → se toma el mayor riesgo")
    void rulesAndGroqDisagree_takesHigherRisk() {
        String debtorId = "5";
        String debtId = "debt-5";
        LocalDate dueDate = LocalDate.of(2026, 3, 1);

        when(debtClient.getDebtsByDebtor(debtorId))
                .thenReturn(List.of(debt(debtId, debtorId, dueDate)));
        when(paymentClient.getPaymentsByDebt(debtId))
                .thenReturn(List.of(payment(debtId, dueDate.plusDays(9)))); // reglas → LOW_RISK
        when(groqAiAnalyzer.analyze(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(groqResponse(RiskLevel.HIGH_RISK, 80.0)); // IA → HIGH_RISK
        when(repo.findByClientId(5L)).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        RiskResponse result = service.recalculate(5L);

        assertThat(result.riskLevel()).isEqualTo(RiskLevel.HIGH_RISK);
    }
}
