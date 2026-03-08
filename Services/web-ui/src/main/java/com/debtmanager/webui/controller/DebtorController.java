package com.debtmanager.webui.controller;

import com.debtmanager.webui.dto.request.DebtorRequest;
import com.debtmanager.webui.service.DebtorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@Controller
@RequestMapping("/debtors")
public class DebtorController {

    private final DebtorService debtorService;

    public DebtorController(DebtorService debtorService) {
        this.debtorService = debtorService;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        String token = (String) session.getAttribute("jwt");
        try {
            model.addAttribute("debtors", debtorService.getAll(token));
        } catch (Exception e) {
            model.addAttribute("debtors", new ArrayList<>());
            model.addAttribute("error", "Servicio no disponible. Los datos aparecerán cuando el servicio esté activo.");
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
            model.addAttribute("debts", new ArrayList<>());
        } catch (Exception e) {
            return "redirect:/debtors";
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
            HttpSession session,
            Model model) {
        try {
            String token = (String) session.getAttribute("jwt");
            DebtorRequest request = new DebtorRequest(name, type, documentType, documentNumber, email, phone);
            debtorService.create(request, token);
            return "redirect:/debtors";
        } catch (Exception e) {
            return "redirect:/dashboard?error=true";
        }
    }
}
