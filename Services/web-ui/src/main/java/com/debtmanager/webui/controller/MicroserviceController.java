package com.debtmanager.webui.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

    @GetMapping
    public String status(Model model) {
        model.addAttribute("gatewayHealthUrl", gatewayHealthUrl);
        model.addAttribute("authHealthUrl", authHealthUrl);
        model.addAttribute("debtorHealthUrl", debtorHealthUrl);
        model.addAttribute("debtHealthUrl", debtHealthUrl);
        model.addAttribute("paymentHealthUrl", paymentHealthUrl);
        model.addAttribute("fxHealthUrl", fxHealthUrl);
        model.addAttribute("aiRiskHealthUrl", aiRiskHealthUrl);
        return "microservices/status";
    }
}
