package com.example.paymentservice.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.paymentservice.dto.response.DebtResponse;
import com.example.paymentservice.dto.response.PaymentResponse;

import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final RestTemplate restTemplate;
    private final String notificationServiceUrl;
    private final String debtorServiceUrl; // ← AGREGAR este campo

    private record DebtorResponse(Long id, String name, String email, String document, String phone) {
    }

    public NotificationClient(
            RestTemplate restTemplate,
            @Value("${app.notification-service.base-url:http://localhost:8085}") String notificationServiceUrl,
            @Value("${app.debtor-service.base-url:http://localhost:8082}") String debtorServiceUrl) { // ← AGREGAR
                                                                                                      // parámetro
        this.restTemplate = restTemplate;
        this.notificationServiceUrl = notificationServiceUrl;
        this.debtorServiceUrl = debtorServiceUrl; // ← AGREGAR asignación
    }

    public void sendPaymentConfirmation(
            PaymentResponse payment,
            DebtResponse debt,
            String debtorName,
            String debtorEmail) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("debtId", payment.debtId());
            body.put("debtorId", debt.debtorId());
            body.put("debtorName", debtorName);
            body.put("debtorEmail", debtorEmail);
            body.put("amountPaid", payment.amount());
            body.put("remainingBalance", debt.currentBalance());
            body.put("currency", debt.currency());
            body.put("paymentDate", payment.paymentDate().toString());

            // Propagar el JWT del request actual
            String token = "";
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                token = attrs.getRequest().getHeader("Authorization");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", token != null ? token : "");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(
                    notificationServiceUrl + "/api/v1/notifications/payment-confirmation",
                    entity,
                    Void.class);

            log.info("Notificación enviada para deuda {}", payment.debtId());

        } catch (Exception ex) {
            log.warn("No se pudo notificar para deuda {}: {}", payment.debtId(), ex.getMessage());
        }
    }

    public String getDebtorName(String debtorId) {
        try {
            DebtorResponse debtor = restTemplate.getForObject(
                    debtorServiceUrl + "/api/v1/debtors/" + debtorId,
                    DebtorResponse.class);
            return debtor != null ? debtor.name() : "Cliente";
        } catch (Exception ex) {
            log.warn("No se pudo obtener nombre del deudor {}: {}", debtorId, ex.getMessage());
            return "Cliente";
        }
    }

    public String getDebtorEmail(String debtorId) {
        try {
            DebtorResponse debtor = restTemplate.getForObject(
                    debtorServiceUrl + "/api/v1/debtors/" + debtorId,
                    DebtorResponse.class);
            return debtor != null ? debtor.email() : "";
        } catch (Exception ex) {
            log.warn("No se pudo obtener email del deudor {}: {}", debtorId, ex.getMessage());
            return "";
        }
    }
}
