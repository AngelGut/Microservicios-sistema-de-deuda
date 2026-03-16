package com.example.airiskservice.model;

/**
 * Niveles de riesgo crediticio del sistema.
 *
 * Reglas de clasificación:
 * - GOOD_CLIENT  → sin retrasos acumulados
 * - LOW_RISK     → retrasos acumulados menores a 30 días
 * - HIGH_RISK    → retrasos acumulados iguales o mayores a 30 días
 */
public enum RiskLevel {
    GOOD_CLIENT,
    LOW_RISK,
    HIGH_RISK
}
