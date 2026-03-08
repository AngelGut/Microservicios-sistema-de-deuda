//ESTE ES EL CONTROLADOR ORIGINAL ANTES DE SUBIR A PRODCCION DESCOMENATARLO Y QUITAR EL CODIGO DE LOGIN TEMPORAL

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
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Credenciales incorrectas");
            return "auth/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }
}

/*
 * //ESTE CONTROLADOR ES SOLO PARA DESARROLLO, HAY QUE QUITARLO O COMENTARLO
 * ANTES DE SUBIR A PRODUCCION
 * package com.debtmanager.webui.controller;
 *
 * import com.debtmanager.webui.service.AuthService;
 * import jakarta.servlet.http.HttpSession;
 * import org.springframework.stereotype.Controller;
 * import org.springframework.ui.Model;
 * import org.springframework.web.bind.annotation.*;
 *
 * @Controller
 *
 * @RequestMapping("/auth")
 * public class AuthController {
 *
 * private final AuthService authService;
 *
 * public AuthController(AuthService authService) {
 * this.authService = authService;
 * }
 *
 * @GetMapping("/login")
 * public String loginPage() {
 * return "auth/login";
 * }
 *
 * @PostMapping("/login")
 * public String login(@RequestParam String email,
 *
 * @RequestParam String password,
 * HttpSession session,
 * Model model) {
 * // try {
 * // String token = authService.login(email, password);
 * // session.setAttribute("jwt", token);
 * // return "redirect:/dashboard";
 * // } catch (Exception e) {
 * // model.addAttribute("error", "Credenciales incorrectas");
 * // return "auth/login";
 * // }
 * session.setAttribute("jwt", "token-temporal-dev");
 * return "redirect:/dashboard";
 * }
 *
 * @GetMapping("/logout")
 * public String logout(HttpSession session) {
 * session.invalidate();
 * return "redirect:/auth/login";
 * }
 * }
 */
