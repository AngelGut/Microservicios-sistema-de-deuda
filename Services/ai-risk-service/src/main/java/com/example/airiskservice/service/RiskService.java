package com.example.airiskservice.service;

import com.example.airiskservice.dto.response.RiskResponse;
import java.util.List;

public interface RiskService {
    RiskResponse getRiskByClient(String clientId);
    List<RiskResponse> getHighRiskClients();
    RiskResponse recalculate(String clientId);
    void recalculateAll();
}
