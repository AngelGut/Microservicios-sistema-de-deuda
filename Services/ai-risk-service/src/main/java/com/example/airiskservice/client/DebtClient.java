package com.example.airiskservice.client;

import com.example.airiskservice.client.fallback.DebtClientFallback;
import com.example.airiskservice.dto.response.DebtDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "debt-service", url = "${app.debt-service.base-url}", fallback = DebtClientFallback.class)
public interface DebtClient {

    @GetMapping("/api/v1/debts")
    DebtListResponse getDebtsByDebtor(@RequestParam("debtorId") String debtorId);

    @JsonIgnoreProperties(ignoreUnknown = true)
    record DebtListResponse(List<DebtDTO> data) {
        public List<DebtDTO> getData() {
            return data != null ? data : List.of();
        }
    }
}
