package com.example.airiskservice.client;

import com.example.common.api.ApiResponse;
import com.example.airiskservice.dto.response.PaymentDTO;
import com.example.airiskservice.client.fallback.PaymentClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "payment-service", url = "${app.payment-service.base-url}", fallback = PaymentClientFallback.class)
public interface PaymentClient {

    @GetMapping("/api/v1/payments")
    ApiResponse<List<PaymentDTO>> getPaymentsByDebt(@RequestParam("debtId") String debtId);
}
