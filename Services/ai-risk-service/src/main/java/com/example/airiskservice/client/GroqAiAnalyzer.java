package com.example.airiskservice.client;

import com.example.airiskservice.dto.response.GroqRiskResponse;
import com.example.airiskservice.dto.response.PaymentHistoryDTO;
import java.util.List;

/**
 * Abstracción del analizador de IA.
 *
 * Principio DIP: RiskServiceImpl depende de esta interfaz,
 *               no de GroqClient directamente.
 * Principio OCP: en el futuro se puede cambiar de Groq a otro
 *               proveedor sin modificar el servicio.
 */
public interface GroqAiAnalyzer {

    /**
     * Analiza el historial de pagos de un cliente y retorna
     * el resultado del análisis de IA.
     *
     * @return GroqRiskResponse o null si el servicio no está disponible.
     */
    GroqRiskResponse analyze(Long clientId,
                              int totalDaysLate,
                              int latePaymentCount,
                              int paymentCount,
                              List<PaymentHistoryDTO> payments);
}
