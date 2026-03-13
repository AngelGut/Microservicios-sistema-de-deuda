package com.debtmanager.webui.controller;

import com.debtmanager.webui.client.AiRiskClient;
import com.debtmanager.webui.dto.request.DebtorRequest;
import com.debtmanager.webui.dto.response.AiRiskResponse;
import com.debtmanager.webui.service.DebtService;
import com.debtmanager.webui.service.DebtorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@Controller
@RequestMapping("/debtors")
public class DebtorController {

    private final DebtorService debtorService;
    private final DebtService debtService;
    private final AiRiskClient aiRiskClient;

    public DebtorController(DebtorService debtorService, DebtService debtService,
            AiRiskClient aiRiskClient) {
        this.debtorService = debtorService;
        this.debtService = debtService;
        this.aiRiskClient = aiRiskClient;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        String token = (String) session.getAttribute("jwt");
        try {
            model.addAttribute("debtors", debtorService.getAll(token));
        } catch (Exception e) {
            model.addAttribute("debtors", new ArrayList<>());
            model.addAttribute("error", "Servicio no disponible.");
        }
        return "debtors/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable String id,
            HttpSession session,
            Model model) {
        String token = (String) session.getAttribute("jwt");
        try {
            model.addAttribute("debtor", debtorService.getById(id, token));
        } catch (Exception e) {
            return "redirect:/debtors";
        }
        try {
            model.addAttribute("debts", debtService.getByDebtorId(id, token));
        } catch (Exception e) {
            model.addAttribute("debts", new ArrayList<>());
        }
        try {
            model.addAttribute("aiRisk", aiRiskClient.recalculate(id, token));
        } catch (Exception e) {
            model.addAttribute("aiRisk", null);
        }
        return "debtors/detail";
    }

    @GetMapping("/new")
    public String createForm() {
        return "debtors/form";
    }

    @PostMapping
    public String create(@RequestParam String name,
            @RequestParam String type,
            @RequestParam String documentType,
            @RequestParam String documentNumber,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            HttpSession session) {
        try {
            String token = (String) session.getAttribute("jwt");
            DebtorRequest request = new DebtorRequest(name, type, documentType, documentNumber, email, phone);
            debtorService.create(request, token);
            return "redirect:/debtors";
        } catch (Exception e) {
            return "redirect:/debtors?error=true";
        }
    }

    @GetMapping("/api/risk/{id}")
    @ResponseBody
    public ResponseEntity<AiRiskResponse> getRiskApi(
            @PathVariable String id,
            HttpSession session) {
        String token = (String) session.getAttribute("jwt");
        AiRiskResponse risk = aiRiskClient.getRiskByDebtorId(id, token);
        return ResponseEntity.ok(risk);
    }
}
