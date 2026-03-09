package com.example.airiskservice.service;

import com.example.airiskservice.dto.response.RiskResponse;
import java.util.List;

public interface RiskService {
    RiskResponse getRiskByClient(Long clientId);
    List<RiskResponse> getHighRiskClients();
    RiskResponse recalculate(Long clientId);
    void recalculateAll();
}
