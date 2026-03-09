package com.example.airiskservice;

import com.example.airiskservice.client.PaymentClient;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskServiceImplTest {

    @Mock private ClientRiskRepository clientRiskRepository;
    @Mock private PaymentClient paymentClient;
    @InjectMocks private RiskServiceImpl riskService;

    @Test
    @DisplayName("Cliente sin pagos tardíos debe ser GOOD_CLIENT")
    void recalculate_noLatePayments_goodClient() {
        PaymentHistoryDTO onTime = new PaymentHistoryDTO(
                1L, 1L, 10L, BigDecimal.valueOf(500),
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 1), null);

        when(paymentClient.getPaymentsByClient(10L)).thenReturn(List.of(onTime));
        when(clientRiskRepository.findByClientId(10L)).thenReturn(Optional.empty());
        when(clientRiskRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RiskResponse result = riskService.recalculate(10L);

        assertThat(result.riskLevel()).isEqualTo(RiskLevel.GOOD_CLIENT);
        assertThat(result.totalDaysLate()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cliente con menos de 30 días de mora debe ser LOW_RISK")
    void recalculate_lowLateDays_lowRisk() {
        PaymentHistoryDTO late = new PaymentHistoryDTO(
                1L, 1L, 20L, BigDecimal.valueOf(500),
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 1), null);

        when(paymentClient.getPaymentsByClient(20L)).thenReturn(List.of(late));
        when(clientRiskRepository.findByClientId(20L)).thenReturn(Optional.empty());
        when(clientRiskRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RiskResponse result = riskService.recalculate(20L);

        assertThat(result.riskLevel()).isEqualTo(RiskLevel.LOW_RISK);
        assertThat(result.totalDaysLate()).isEqualTo(9);
    }

    @Test
    @DisplayName("Cliente con 30 o más días de mora debe ser HIGH_RISK")
    void recalculate_highLateDays_highRisk() {
        PaymentHistoryDTO late = new PaymentHistoryDTO(
                1L, 1L, 30L, BigDecimal.valueOf(500),
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 3, 1), null);

        when(paymentClient.getPaymentsByClient(30L)).thenReturn(List.of(late));
        when(clientRiskRepository.findByClientId(30L)).thenReturn(Optional.empty());
        when(clientRiskRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RiskResponse result = riskService.recalculate(30L);

        assertThat(result.riskLevel()).isEqualTo(RiskLevel.HIGH_RISK);
        assertThat(result.totalDaysLate()).isGreaterThanOrEqualTo(30);
    }

    @Test
    @DisplayName("Sin historial de pagos (payment-service caído) debe ser GOOD_CLIENT")
    void recalculate_emptyPayments_fallback_goodClient() {
        when(paymentClient.getPaymentsByClient(40L)).thenReturn(List.of());
        when(clientRiskRepository.findByClientId(40L)).thenReturn(Optional.empty());
        when(clientRiskRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RiskResponse result = riskService.recalculate(40L);

        assertThat(result.riskLevel()).isEqualTo(RiskLevel.GOOD_CLIENT);
        assertThat(result.riskScore()).isEqualTo(0.0);
    }
}
