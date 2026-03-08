package com.debtmanager.webui.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 401 — token expirado, redirige al login
    @ExceptionHandler(HttpClientErrorException.Unauthorized.class)
    public String handleUnauthorized() {
        return "redirect:/auth/login";
    }

    // 403 — sin permiso
    @ExceptionHandler(HttpClientErrorException.Forbidden.class)
    public String handleForbidden(Model model) {
        model.addAttribute("error", "No tienes permiso para esta acción");
        return "error/403";
    }

    // 404 — no encontrado
    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public String handleNotFound(Model model) {
        model.addAttribute("error", "El recurso no fue encontrado");
        return "error/404";
    }

    // Servicio caído
    @ExceptionHandler(ResourceAccessException.class)
    public String handleServiceDown(Model model) {
        model.addAttribute("error", "Servicio no disponible. Intenta más tarde.");
        return "error/503";
    }
}
