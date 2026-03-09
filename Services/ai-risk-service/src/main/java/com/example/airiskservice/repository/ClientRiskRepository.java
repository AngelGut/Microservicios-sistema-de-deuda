package com.example.airiskservice.repository;

import com.example.airiskservice.model.ClientRisk;
import com.example.airiskservice.model.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para ClientRisk.
 *
 * Principio ISP: expone solo operaciones relevantes al dominio de riesgo.
 */
@Repository
public interface ClientRiskRepository extends JpaRepository<ClientRisk, Long> {

    Optional<ClientRisk> findByClientId(Long clientId);

    List<ClientRisk> findByRiskLevel(RiskLevel riskLevel);

    boolean existsByClientId(Long clientId);
}
