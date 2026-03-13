package com.debtmanager.webui.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/microservices")
public class MicroserviceController {

    @Value("${services.health.gateway-url}")
    private String gatewayHealthUrl;

    @Value("${services.health.auth-url}")
    private String authHealthUrl;

    @Value("${services.health.debtor-url}")
    private String debtorHealthUrl;

    @Value("${services.health.debt-url}")
    private String debtHealthUrl;

    @Value("${services.health.payment-url}")
    private String paymentHealthUrl;

    @Value("${services.health.fx-url}")
    private String fxHealthUrl;

    @Value("${services.health.ai-url}")
    private String aiRiskHealthUrl;

    @Value("${services.health.notification-url}")
    private String notificationHealthUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping
    public String status(Model model) {
        return "microservices/status";
    }

    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, String> services = new LinkedHashMap<>();
        services.put("gateway", gatewayHealthUrl);
        services.put("auth", authHealthUrl);
        services.put("debtor", debtorHealthUrl);
        services.put("debt", debtHealthUrl);
        services.put("payment", paymentHealthUrl);
        services.put("fx", fxHealthUrl);
        services.put("notification", notificationHealthUrl);
        services.put("ai", aiRiskHealthUrl);

        Map<String, Object> results = new LinkedHashMap<>();
        int upCount = 0;

        for (Map.Entry<String, String> entry : services.entrySet()) {
            String key = entry.getKey();
            String url = entry.getValue();
            long start = System.currentTimeMillis();
            try {
                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
                long latency = System.currentTimeMillis() - start;
                boolean isUp = response.getStatusCode().is2xxSuccessful();
                String status = isUp ? (latency > 500 ? "slow" : "up") : "down";
                if (isUp)
                    upCount++;
                results.put(key, Map.of("status", status, "latency", latency));
            } catch (Exception e) {
                results.put(key, Map.of("status", "down", "latency", -1));
            }
        }

        results.put("upCount", upCount);
        results.put("total", services.size());
        return ResponseEntity.ok(results);
    }
}
