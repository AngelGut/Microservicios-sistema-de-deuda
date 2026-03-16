package com.example.airiskservice.client;

import com.example.airiskservice.client.fallback.PaymentClientFallback;
import com.example.airiskservice.dto.response.PaymentDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "payment-service", url = "${app.payment-service.base-url}", fallback = PaymentClientFallback.class)
public interface PaymentClient {

    @GetMapping("/api/v1/payments")
    PaymentListResponse getPaymentsByDebt(@RequestParam("debtId") String debtId);

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PaymentListResponse(List<PaymentDTO> data) {
        public List<PaymentDTO> getData() {
            return data != null ? data : List.of();
        }
    }
}
