package com.debtmanager.webui.controller;

import com.debtmanager.webui.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

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
            log.info("🔐 Intentando login para: {}", email);
            String token = authService.login(email, password);
            log.info("✅ Login exitoso para: {}", email);
            session.setAttribute("jwt", token);
            String displayName = email != null && email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
            session.setAttribute("userDisplayName", displayName == null || displayName.isBlank() ? "Usuario" : displayName);
            return "redirect:/dashboard";
        } catch (Exception e) {
            log.error("❌ Error en login para {}: {} - {}", email, e.getClass().getSimpleName(), e.getMessage());
            model.addAttribute("error",
                    "Credenciales incorrectas. Verifica tu email y contraseña.");
            return "auth/login";
        }
    }

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
            log.error("❌ Error en registro: {}", e.getMessage());
            model.addAttribute("registerError",
                    "No se pudo crear la cuenta. " + e.getMessage());
            return "auth/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }
}
