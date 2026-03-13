package com.debtmanager.webui.service;

import com.debtmanager.webui.client.PaymentClient;
import com.debtmanager.webui.dto.request.PaymentRequest;
import com.debtmanager.webui.dto.response.PaymentResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private final PaymentClient paymentClient;

    public PaymentService(PaymentClient paymentClient) {
        this.paymentClient = paymentClient;
    }

    public List<PaymentResponse> getAll(String token) {
        return paymentClient.getAll(token);
    }

    public List<PaymentResponse> getByDebtId(String debtId, String token) {
        return paymentClient.getByDebtId(debtId, token);
    }

    public void create(PaymentRequest request, String token) {
        paymentClient.create(request, token);
    }
}
