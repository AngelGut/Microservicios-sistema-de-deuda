package com.debtmanager.webui.controller;

import com.debtmanager.webui.dto.request.PaymentRequest;
import com.debtmanager.webui.dto.response.DebtResponse;
import com.debtmanager.webui.service.DebtService;
import com.debtmanager.webui.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final DebtService debtService;

    public PaymentController(PaymentService paymentService,
            DebtService debtService) {
        this.paymentService = paymentService;
        this.debtService = debtService;
    }

    // ── Lista global de todos los pagos ──
    @GetMapping
    public String list(HttpSession session, Model model) {
        String token = (String) session.getAttribute("jwt");

        // Historial de pagos
        try {
            model.addAttribute("payments", paymentService.getAll(token));
        } catch (Exception e) {
            model.addAttribute("payments", new ArrayList<>());
            model.addAttribute("error",
                    "Servicio no disponible. Los datos aparecerán cuando payment-service esté activo.");
        }

        // Deudas activas para el selector del modal
        try {
            List<DebtResponse> activas = debtService.getAll(token)
                    .stream()
                    .filter(d -> "ACTIVA".equals(d.status()))
                    .toList();
            model.addAttribute("activeDebts", activas);
        } catch (Exception e) {
            model.addAttribute("activeDebts", new ArrayList<>());
        }

        return "payments/list";
    }

    // ── Registrar pago (POST desde modal) ──
    @PostMapping
    public String create(@RequestParam String debtId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) String notes,
            HttpSession session) {
        try {
            String token = (String) session.getAttribute("jwt");
            PaymentRequest request = new PaymentRequest(debtId, amount, reference, notes);
            paymentService.create(request, token);
            return "redirect:/payments?success=true";
        } catch (Exception e) {
            return "redirect:/payments?error=true";
        }
    }
}
