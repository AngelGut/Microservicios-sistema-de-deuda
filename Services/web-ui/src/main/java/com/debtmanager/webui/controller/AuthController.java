package com.debtmanager.webui.controller;

import com.debtmanager.webui.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        try {
            String token = authService.login(email, password);
            session.setAttribute("jwt", token);
            String displayName = email != null && email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
            session.setAttribute("userDisplayName", displayName == null || displayName.isBlank() ? "Usuario" : displayName);
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error",
                    "Credenciales incorrectas. Verifica tu email y contraseña.");
            return "auth/login";
        }
    }

    // ── REGISTRO ─────────────────────────────────────────────
    @PostMapping("/register")
    public String register(@RequestParam String fullName,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {
        try {
            authService.register(fullName, username, email, password);
            model.addAttribute("registerSuccess",
                    "¡Cuenta creada exitosamente! Ya puedes iniciar sesión.");
            return "auth/login";
        } catch (Exception e) {
            model.addAttribute("registerError",
                    "No se pudo crear la cuenta. " + e.getMessage());
            return "auth/login";
        }
    }

    // ── LOGOUT ───────────────────────────────────────────────
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }
}
