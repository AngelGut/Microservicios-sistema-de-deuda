package com.debtmanager.webui.controller;

import com.debtmanager.webui.service.DashboardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String token = (String) session.getAttribute("jwt");

        try {
            Map<String, Object> data = dashboardService.getDashboardData(token);
            data.forEach(model::addAttribute);
        } catch (Exception e) {
            model.addAttribute("dashboardError",
                    "No se pudo conectar con los servicios. Verifica que el gateway esté activo.");
        }

        return "dashboard/index";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }
}
