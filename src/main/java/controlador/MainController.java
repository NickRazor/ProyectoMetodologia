package controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        // Si hay una sesión activa, agregar los datos del usuario al modelo
        if (session.getAttribute("nombreUsuario") != null) {
            model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
        }
        return "index";
    }
}