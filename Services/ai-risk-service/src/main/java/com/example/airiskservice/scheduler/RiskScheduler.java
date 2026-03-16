package com.example.airiskservice.scheduler;

import com.example.airiskservice.service.RiskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job programado que recalcula el riesgo de todos los clientes diariamente.
 *
 * Principio SRP: su única responsabilidad es disparar el recálculo en el horario definido.
 */
@Component
public class RiskScheduler {

    private static final Logger log = LoggerFactory.getLogger(RiskScheduler.class);

    private final RiskService riskService;

    public RiskScheduler(RiskService riskService) {
        this.riskService = riskService;
    }

    /**
     * Se ejecuta todos los días a las 2:00 AM.
     * Recalcula el riesgo de todos los clientes registrados.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void dailyRiskRecalculation() {
        log.info("Iniciando recálculo diario de riesgo crediticio...");
        try {
            riskService.recalculateAll();
            log.info("Recálculo diario completado exitosamente.");
        } catch (Exception e) {
            log.error("Error en recálculo diario de riesgo: {}", e.getMessage(), e);
        }
    }
}
