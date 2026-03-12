package com.debtmanager.webui.controller;

import com.debtmanager.webui.dto.request.DebtRequest;
import com.debtmanager.webui.dto.response.DebtResponse;
import com.debtmanager.webui.dto.response.PaymentResponse;
import com.debtmanager.webui.service.DebtService;
import com.debtmanager.webui.service.DebtorService;
import com.debtmanager.webui.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/debts")
public class DebtController {

    private final DebtService debtService;
    private final DebtorService debtorService;
    private final PaymentService paymentService;

    public DebtController(DebtService debtService,
            DebtorService debtorService,
            PaymentService paymentService) {
        this.debtService = debtService;
        this.debtorService = debtorService;
        this.paymentService = paymentService;
    }

    // ── Lista global de todas las deudas ──
    @GetMapping
    public String list(HttpSession session, Model model) {
        String token = (String) session.getAttribute("jwt");
        try {
            model.addAttribute("debts", debtService.getAll(token));
        } catch (Exception e) {
            model.addAttribute("debts", new ArrayList<>());
            model.addAttribute("error",
                    "Servicio no disponible. Los datos aparecerán cuando debt-service esté activo.");
        }
        try {
            model.addAttribute("allDebtors", debtorService.getAll(token));
        } catch (Exception e) {
            model.addAttribute("allDebtors", new ArrayList<>());
        }
        return "debts/list";
    }

    // ── Detalle de una deuda (redirige a lista por ahora) ──
    @GetMapping("/{id}")
    public String detail(@PathVariable String id,
            HttpSession session,
            Model model) {
        String token = (String) session.getAttribute("jwt");
        try {
            DebtResponse debt = debtService.getById(id, token);
            model.addAttribute("debt", debt);
            try {
                model.addAttribute("payments", paymentService.getByDebtId(id, token));
            } catch (Exception e) {
                model.addAttribute("payments", new ArrayList<>());
            }
        } catch (Exception e) {
            return "redirect:/debts";
        }
        return "debts/list";
    }

    // ── Formulario nueva deuda (viene desde debtors/detail) ──
    @GetMapping("/new")
    public String createForm(@RequestParam String debtorId,
            HttpSession session,
            Model model) {
        model.addAttribute("debtorId", debtorId);
        try {
            String token = (String) session.getAttribute("jwt");
            model.addAttribute("allDebtors", debtorService.getAll(token));
        } catch (Exception e) {
            model.addAttribute("allDebtors", new ArrayList<>());
        }
        return "debts/form";
    }

    // ── Crear deuda ──
    @PostMapping
    public String create(@RequestParam String debtorId,
            @RequestParam String description,
            @RequestParam BigDecimal originalAmount,
            @RequestParam String currency,
            @RequestParam(required = false) String dueDate,
            HttpSession session) {
        try {
            String token = (String) session.getAttribute("jwt");
            DebtRequest request = new DebtRequest(debtorId, description, originalAmount, currency, dueDate);
            debtService.create(request, token);
            return "redirect:/debtors/" + debtorId;
        } catch (Exception e) {
            return "redirect:/debts?error=true";
        }
    }

    // ── API: historial de pagos por deuda (llamado desde JS) ──
    @GetMapping("/payments/by-debt/{debtId}")
    @ResponseBody
    public ResponseEntity<List<PaymentResponse>> paymentsByDebt(@PathVariable String debtId,
            HttpSession session) {
        String token = (String) session.getAttribute("jwt");
        try {
            List<PaymentResponse> payments = paymentService.getByDebtId(debtId, token);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
}
