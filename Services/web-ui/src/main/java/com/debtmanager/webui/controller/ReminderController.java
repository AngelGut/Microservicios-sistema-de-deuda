package com.debtmanager.webui.controller;

import com.debtmanager.webui.dto.response.DebtResponse;
import com.debtmanager.webui.dto.response.ReminderResponse;
import com.debtmanager.webui.service.DebtService;
import com.debtmanager.webui.service.ReminderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/reminders")
public class ReminderController {

    private final ReminderService reminderService;
    private final DebtService debtService;
    private final ObjectMapper objectMapper;

    public ReminderController(ReminderService reminderService,
            DebtService debtService,
            ObjectMapper objectMapper) {
        this.reminderService = reminderService;
        this.debtService = debtService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        String token = (String) session.getAttribute("jwt");

        // Recordatorios próximos (30 días)
        List<ReminderResponse> reminders = new ArrayList<>();
        try {
            reminders = reminderService.getUpcoming(30, token);
        } catch (Exception e) {
            model.addAttribute("error",
                    "Servicio no disponible. Los recordatorios aparecerán cuando notification-service esté activo.");
        }
        model.addAttribute("reminders", reminders);

        // JSON para el calendario JS
        try {
            model.addAttribute("remindersJson", objectMapper.writeValueAsString(reminders));
        } catch (Exception e) {
            model.addAttribute("remindersJson", "[]");
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

        return "reminders/list";
    }

    // POST — cuando notification-service implemente creación de recordatorios
    @PostMapping
    public String create(@RequestParam String debtId,
            @RequestParam String description,
            @RequestParam String dueDate,
            @RequestParam(required = false, defaultValue = "3") int daysBefore,
            HttpSession session) {
        try {
            String token = (String) session.getAttribute("jwt");
            reminderService.create(debtId, description, dueDate, daysBefore, token);
            return "redirect:/reminders?success=true";
        } catch (Exception e) {
            return "redirect:/reminders?error=true";
        }
    }
}
