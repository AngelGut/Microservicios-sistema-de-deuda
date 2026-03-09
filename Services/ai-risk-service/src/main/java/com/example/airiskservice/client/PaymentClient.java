package com.example.airiskservice.client;

import com.example.airiskservice.dto.response.PaymentHistoryDTO;
import com.example.airiskservice.client.fallback.PaymentClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(
        name = "payment-service",
        url = "${app.payment-service.base-url}",
        fallback = PaymentClientFallback.class
)
public interface PaymentClient {

    @GetMapping("/payments/by-client/{clientId}")
    List<PaymentHistoryDTO> getPaymentsByClient(@PathVariable Long clientId);
}
